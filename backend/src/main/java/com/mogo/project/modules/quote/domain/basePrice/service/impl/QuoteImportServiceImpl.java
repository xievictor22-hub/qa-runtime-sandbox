package com.mogo.project.modules.quote.domain.basePrice.service.impl;



import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;


import com.mogo.project.common.exception.ImportValidationException;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.common.util.RangeUtil;
import com.mogo.project.modules.oss.service.ISysFileService;
import com.mogo.project.modules.quote.domain.basePrice.convert.QuoteSourceExportConvert;
import com.mogo.project.modules.quote.domain.basePrice.convert.QuoteSourceImportConvert;
import com.mogo.project.modules.quote.domain.basePrice.dto.QuoteBasePriceImportErrorDto;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteImportSource;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteVersionLog;
import com.mogo.project.modules.quote.domain.basePrice.manager.ProductLibraryManager;
import com.mogo.project.modules.quote.domain.basePrice.manager.QuoteRuleCacheManager;
import com.mogo.project.modules.quote.domain.basePrice.service.*;
import com.mogo.project.modules.quote.domain.basePrice.dto.QuoteBasePriceImportExcelDto;
import com.mogo.project.modules.quote.domain.basePrice.vo.QuoteBasePriceExportExcelVo;
import com.mogo.project.modules.quote.core.component.QuoteDataProcessService;
import com.mogo.project.modules.quote.domain.order.service.IQuoteDetailItemService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 导入报价底表并且转化成双表，维护报价底表
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteImportServiceImpl implements IQuoteImportService {

    private final IQuoteImportSourceService quoteImportSourceService;
    private final IQuoteProductLibraryService quoteProductLibraryService;
    private final IQuoteFoldingRuleService quoteFoldingRuleService;
    private final IQuoteVersionLogService quoteVersionLogService;
    private final IQuoteDetailItemService quoteDetailItemService;
    private final QuoteDataProcessService quoteDataProcessService; // 注入清洗服务
    private final QuoteSourceExportConvert quoteSourceExportConvert;//导出转换类
    private final QuoteSourceImportConvert quoteSourceImportConvert;//导入转换类
    private final QuoteRuleCacheManager quoteRuleCacheManager;//底价缓存管理类
    private final ProductLibraryManager productLibraryManager;//制品低价库缓存redis管理器

    // 1. 引入校验器
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    // 2. 引入文件存储服务 (假设你封装了 MinIO)
    private final ISysFileService sysFileService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importQuoteExcel(MultipartFile file, String version, String description) {


        // 2. 智能版本检查与删除
        checkAndSmartDeleteVersion(version);
        List<QuoteBasePriceImportErrorDto> list;

        // 3. 读取 Excel 并落库到 Source 表
        try {
            //从边读边用监听类处理转成先读，再验证再处理
            list = EasyExcel.read(file.getInputStream()).head(QuoteBasePriceImportErrorDto.class)
                    .sheet().doReadSync();
        } catch (IOException e) {
            log.error("Excel读取异常", e);
            throw new ServiceException("Excel 读取失败: " + e.getMessage());
        }
        if(list.isEmpty())throw new ServiceException("导入表不能为空");
        // 填充行号 (i 就是顺序)
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSort(i); // 记录它是第几行
        }

        ArrayList<QuoteImportSource> successEntities = new ArrayList<>();
        boolean hasError = false;

        // 先执行列表内排重校验
        checkListDuplicates(list);

        // 1. 生成批次ID
        String batchId = UUID.randomUUID().toString().replace("-", "");

        for (QuoteBasePriceImportErrorDto dto : list) {
            StringBuilder rowErrorMsg = new StringBuilder();
            //基础校验类上的非空与最大最小值
            Set<ConstraintViolation<QuoteBasePriceImportErrorDto>> validate = validator.validate(dto);
            for (ConstraintViolation<QuoteBasePriceImportErrorDto> v : validate) {
                rowErrorMsg.append(v.getMessage()).append("; ");
            }
            // 4.2 业务校验 (RangeUtil)
            String logicError = checkLogic(dto);
            if (logicError != null) {
                rowErrorMsg.append(logicError);
            }
            if (!rowErrorMsg.isEmpty()) {
                hasError = true;
                dto.setErrorMsg(rowErrorMsg.toString());
            }else {
                //没报错转成实体准备入库
                QuoteImportSource source = quoteSourceImportConvert.toEntity(dto);
                source.setVersion(version);
                source.setBatchId(batchId);
                if (source.getQuoteFormula() == null) source.setQuoteFormula(BigDecimal.ONE);
                successEntities.add(source);
            }
        }
        // 5. 决策：只要有 1 行出错，就全部回滚，并返回包含所有数据的 Excel
        if (hasError) {
            // 直接把刚才处理过的 list 导出去！
            // 这样用户能看到：哪些行有错（有红色提示），哪些行没错（errorMsg为空）
            handleErrorExport(list);
        }
        // 6. 全都没错 -> 批量入库
        quoteImportSourceService.saveBatch(successEntities);

        // 7. 清洗分发
        quoteDataProcessService.processAndDistribute(successEntities);
        // 8. 记录日志
        saveVersionLog(version, description, successEntities.size());
        quoteRuleCacheManager.refreshVersion(version);//删除底价的缓存，下次重新获取
        productLibraryManager.clearProcessTreeCache(version);//删除缓存的制品低价库选项
    }

    @Override
    public void downLoadQuoteExcel(String version, HttpServletResponse response) {
        try {
            // 1. 构建查询条件 (使用 MP 的 LambdaQueryWrapper)
            if (StringUtils.isBlank(version)) {
                throw new ServiceException("导出文件失败！，版本不能为空");
            }
            List<QuoteImportSource> list = quoteImportSourceService.selectByVersion(version);
            List<QuoteBasePriceExportExcelVo> exportData = quoteSourceExportConvert.toVoList(list);
            // 3. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 防止中文文件名乱码
            String fileName = URLEncoder.encode("报价底表导出_" + LocalDate.now(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 4. 写出 Excel
            EasyExcel.write(response.getOutputStream(), QuoteBasePriceExportExcelVo.class)
                    .sheet("底价库")
                    .doWrite(exportData);
        }catch (IOException e ){
            log.error("导出失败",e);
            throw  new ServiceException("导出文件失败！");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(QuoteBasePriceImportExcelDto quoteBasePriceImportExcelDto,String version) {
        QuoteVersionLog existLog = quoteVersionLogService.selectByVersion(version);
        if (existLog == null) throw new ServiceException("无当前报价版本，无法添加");

        //校验是否重复
        LambdaQueryWrapper<QuoteImportSource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuoteImportSource::getProjectType, quoteBasePriceImportExcelDto.getProjectType())
                .eq(QuoteImportSource::getProcess1, quoteBasePriceImportExcelDto.getProcess1())
                .eq(QuoteImportSource::getProcess2, quoteBasePriceImportExcelDto.getProcess2())
                .eq(QuoteImportSource::getProcess3, quoteBasePriceImportExcelDto.getProcess3())
                .eq(QuoteImportSource::getProcess4, quoteBasePriceImportExcelDto.getProcess4());
        long count = quoteImportSourceService.count(queryWrapper);
        if (count > 0) throw new ServiceException("当前底价信息重复，无法添加");

        QuoteImportSource entity = quoteSourceImportConvert.toEntity(quoteBasePriceImportExcelDto);
        String batchId = UUID.randomUUID().toString().replace("-", "");
        entity.setVersion(version);
        entity.setBatchId(batchId);
        //转化并一起插入
        ArrayList<QuoteImportSource> quoteImportSources = new ArrayList<>();
        quoteImportSources.add(entity);
        quoteImportSourceService.save(entity);//添加原表
        try {
            quoteDataProcessService.processAndDistribute(quoteImportSources);//分化并分别添加
        }catch (Exception e){
            log.error("分化失败,元数据：{},",quoteBasePriceImportExcelDto,e);
            throw new ServiceException("分化数据出错添加失败");
        }
        quoteRuleCacheManager.refreshVersion(version);//删除底价的缓存，下次重新获取
        productLibraryManager.clearProcessTreeCache(version);//删除缓存的制品低价库选项
    }

    /**
     * 智能删除逻辑：检查引用，决定是软删还是物理删
     */
    private void checkAndSmartDeleteVersion(String version) {
        QuoteVersionLog existLog = quoteVersionLogService.selectByVersion(version);
        if (existLog == null) return;

        // 检查 QuoteDetailItem 是否引用了该版本
        Long usageCount = quoteDetailItemService.countByVersion(version);

        if (usageCount > 0) {
            log.info("版本 {} 已被引用 {} 次，执行软删除。", version, usageCount);
            // 标记产品库和规则库为软删除 (UPDATE set delete_flag=1)
            quoteProductLibraryService.deleteByVersion(version);
            quoteFoldingRuleService.deleteByVersion(version);
            // 原始 Source 表通常不需要保留历史，执行物理删除
            quoteImportSourceService.deleteByVersionPhysically(version);
        } else {
            log.info("版本 {} 未被引用，执行物理删除。", version);
            quoteFoldingRuleService.deleteByVersionPhysically(version);
            quoteProductLibraryService.deleteByVersionPhysically(version);
            quoteImportSourceService.deleteByVersionPhysically(version);
        }
        // 删除旧日志
        quoteVersionLogService.deleteByIdPhysically(existLog.getId());
        //删除当前版本的缓存价格

    }

    private void saveVersionLog(String version, String desc, int count) {
        QuoteVersionLog log = new QuoteVersionLog();
        log.setVersion(version);
        log.setChangeDesc(desc);
        log.setRecordCount(count);
        log.setPublishTime(LocalDateTime.now());
        log.setPublishType(1);
        log.setStatus(1);
        quoteVersionLogService.save(log);
    }

    /**
     * 内部类：Excel 监听器
     */
    private class ExcelBatchListener implements ReadListener<QuoteBasePriceImportExcelDto> {
        private static final int BATCH_COUNT = 1000;
        private List<QuoteImportSource> cachedData = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        private final String version;
        private final String batchId;

        public ExcelBatchListener(String version, String batchId) {
            this.version = version;
            this.batchId = batchId;
        }

        @Override
        public void invoke(QuoteBasePriceImportExcelDto data, AnalysisContext context) {
            QuoteImportSource source = quoteSourceImportConvert.toEntity(data);
            source.setVersion(version);
            source.setBatchId(batchId);
            // 默认值处理
            if (source.getQuoteFormula() == null) source.setQuoteFormula(BigDecimal.ONE);

            cachedData.add(source);
            if (cachedData.size() >= BATCH_COUNT) saveData();
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            saveData();
        }

        private void saveData() {
            if (!cachedData.isEmpty()) {
                // 这里为了演示方便直接循环插入，建议生产环境使用 mybatis-plus 的 saveBatch
                for (QuoteImportSource src : cachedData) {
                    if (src.getCreateTime() == null) src.setCreateTime(LocalDateTime.now());
                    if (src.getDeleteFlag() == null) src.setDeleteFlag(0);
                }
                quoteImportSourceService.saveBatch(cachedData);
                cachedData.clear();
//                cachedData.forEach(sourceMapper::insert);
//                cachedData = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
            }
        }
    }

    /**
     * 业务逻辑校验：检查 RangeUtil 是否能解析
     */
    private String checkLogic(QuoteBasePriceImportExcelDto dto) {
        StringBuilder sb = new StringBuilder();
        // 校验范围格式 C/W/D
        if (!checkRangeFormat(dto.getRangeValC())) sb.append("C值范围格式错误; ");
        if (!checkRangeFormat(dto.getRangeValW())) sb.append("W值范围格式错误; ");
        if (!checkRangeFormat(dto.getRangeValD())) sb.append("D值范围格式错误; ");

        return !sb.isEmpty() ? sb.toString() : null;
    }

    /**
     * 查看范围格式是否正常
     */
    private boolean checkRangeFormat(String rangeStr) {
        if (rangeStr == null || rangeStr.isBlank()) return true; // 空值允许
        return RangeUtil.parse(rangeStr).isValid(); // 复用 RangeUtil 的校验能力
    }

    /**
     * 处理导出
     * @param list
     */
    private void handleErrorExport(List<QuoteBasePriceImportErrorDto> list) {
        String fileName = "导入失败报告_" + System.currentTimeMillis() + ".xlsx";
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // 生成 Excel 到内存流
            EasyExcel.write(out, QuoteBasePriceImportErrorDto.class)
                    .sheet("错误详情")
                    .doWrite(list);
            // 🚀 直接调用 Service 上传字节数组， 获取 URL
            String fileUrl = sysFileService.upload(out.toByteArray(), fileName);
            // 抛出带 URL 的异常
            throw new ImportValidationException(fileUrl, "导入数据存在错误，请下载报告修改后重新上传");
        } catch (IOException e) {
            throw new ServiceException("生成错误报告失败");
        }
    }

    /**
     * 校验列表内部重复
     * 规则：Project Type + Process 1/2/3/4 完全一致视为重复
     */
    private void checkListDuplicates(List<QuoteBasePriceImportErrorDto> list) {
        // Key: 唯一组合键, Value: 首次出现的行号(下标+1)

        Map<String, Integer> seenMap = new HashMap<>(list.size());

        for (int i = 0; i < list.size(); i++) {
            QuoteBasePriceImportErrorDto dto = list.get(i);
            int currentRowNum = i + 1; // 实际 Excel 行号通常是 i+2 (因为有表头)，这里为了简单暂用 i+1

            // 1. 构建唯一 Key (注意处理 null 值，避免 "null" 字符串和空字符串混淆，通常建议 trim)
            String uniqueKey = buildUniqueKey(dto);

            // 2. 检查 Map 中是否存在
            if (seenMap.containsKey(uniqueKey)) {
                Integer firstRowNum = seenMap.get(uniqueKey);

                // 3. 记录错误信息
                String msg = "数据重复：与第 " + firstRowNum + " 行内容一致";
                appendErrorMsg(dto, msg);

            } else {
                // 4. 首次出现，放入 Map
                seenMap.put(uniqueKey, currentRowNum);
            }
        }
    }

    /**
     * 构建唯一键辅助方法
     * 格式示例：1_吊顶_龙骨_一级_无
     */
    private String buildUniqueKey(QuoteBasePriceImportErrorDto dto) {
        return new StringBuilder()
                .append(dto.getProjectType()).append("_")
                .append(trim(dto.getProcess1())).append("_")
                .append(trim(dto.getProcess2())).append("_")
                .append(trim(dto.getProcess3())).append("_")
                .append(trim(dto.getProcess4()))
                .toString();
    }

    /**
     * 字符串防空处理
     */
    private String trim(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * 追加错误信息辅助方法
     */
    private void appendErrorMsg(QuoteBasePriceImportErrorDto dto, String msg) {
        if (dto.getErrorMsg() == null || dto.getErrorMsg().isEmpty()) {
            dto.setErrorMsg(msg);
        } else {
            dto.setErrorMsg(dto.getErrorMsg() + "; " + msg);
        }
    }
}
