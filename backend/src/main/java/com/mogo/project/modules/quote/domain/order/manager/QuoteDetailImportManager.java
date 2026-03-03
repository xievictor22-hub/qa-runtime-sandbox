package com.mogo.project.modules.quote.domain.order.manager;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mogo.project.common.exception.ImportValidationException;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.modules.oss.service.ISysFileService;
import com.mogo.project.modules.quote.domain.order.dto.QuoteDetailFolderImportExcelDto;
import com.mogo.project.modules.quote.domain.order.dto.QuoteDetailProductImportExcelDto;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetailItem;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import com.mogo.project.modules.quote.domain.order.service.IQuoteDetailItemService;
import com.mogo.project.modules.quote.domain.order.service.IQuoteDetailService;
import com.mogo.project.modules.quote.domain.order.service.IQuoteOrderService;
import com.mogo.project.modules.quote.domain.order.service.component.QuoteCalculationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuoteDetailImportManager {

    private final IQuoteDetailService quoteDetailService;
    private final IQuoteDetailItemService quoteDetailItemService;
    private final IQuoteOrderService quoteOrderService;
    private final QuoteCalculationService quoteCalculationService;
    private final ISysFileService sysFileService; // 之前封装的文件上传服务
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();//校验器

    /**
     * 导入 Excel 明细 (全量覆盖当前版本)
     */
    @Transactional(rollbackFor = Exception.class)
    public void importQuoteDetail(Long quoteId, MultipartFile file) {
        // 1. 校验单据状态
        QuoteOrder order = quoteOrderService.getById(quoteId);
        if (order == null) throw new ServiceException("报价单不存在");
        Integer projectType = order.getProjectType();
        if (null==projectType) {
            throw new ServiceException("无项目类型,无法匹配底价表！");
        }
        // 假设只允许草稿或待重新报价状态导入
        if (!"0".equals(order.getStatus()) && !"4".equals(order.getStatus())) {
            throw new ServiceException("当前状态不允许导入明细！");
        }
        // 2. 清理当前版本的旧数据 (覆盖模式)
        // 注意：先删子表，再删父表 (虽然有外键约束通常是级联的，但代码逻辑上显式删除更安全)
        Integer version = order.getCurrentQuoteVersion();

        List<QuoteDetailFolderImportExcelDto> foldingList;
        List<QuoteDetailProductImportExcelDto> productList;

        try(ExcelReader excelReader = EasyExcel.read(file.getInputStream()).build()) {
            // 同步读取所有数据到内存
            foldingList = EasyExcel.read(file.getInputStream()).sheet(0).head(QuoteDetailFolderImportExcelDto.class).doReadSync();
            productList = EasyExcel.read(file.getInputStream()).sheet(1).head(QuoteDetailProductImportExcelDto.class).doReadSync();
        } catch (IOException e) {
            log.error("读取失败",e);
            throw new ServiceException("读取excel文件失败");
        }
        //校验与计算
        ArrayList<QuoteDetail> validDetails = new ArrayList<>();
        boolean hasError = false;
        for (QuoteDetailFolderImportExcelDto folderDto : foldingList) {
            String error = validateAndCalc(folderDto, (d) -> quoteCalculationService.calculateFolding(d, quoteId,version,projectType,productList.size()), validDetails,projectType);
            if (error != null) {
                hasError = true;
                folderDto.setOtherRemark(error);
            }
        }

        // 3.2 处理制品 (Sheet 2)
        for (QuoteDetailProductImportExcelDto productDto : productList) {
            String error = validateAndCalc(productDto, (d) -> quoteCalculationService.calculateProduct(d, quoteId,version,projectType), validDetails,projectType);
            if (error != null) {
                hasError = true;
                productDto.setOtherRemark(error);
            }
        }
        // --- 4. 决策：报错回滚 OR 入库 ---
        if (hasError) {
            // 生成包含两个 Sheet 的错误报告
            handleMultiSheetErrorExport(foldingList, productList);
        }

        // --- 5. 执行入库 ---
        // 5.1 清理旧数据
        cleanOldData(quoteId, version);

        // 5.2 批量保存
        quoteDetailService.saveBatch(validDetails);


    }

    /**
     * 删除旧版本的清单明细
     * @param quoteId
     * @param version
     */
    private void cleanOldData(Long quoteId, Integer version) {
        List<QuoteDetail> oldDetails = quoteDetailService.list(new LambdaQueryWrapper<QuoteDetail>()
                .select(QuoteDetail::getId)
                .eq(QuoteDetail::getQuoteId, quoteId)
                .eq(QuoteDetail::getDetailVersion, version));
        if (!oldDetails.isEmpty()) {
            List<Long> oldDetailIds = oldDetails.stream().map(QuoteDetail::getId).toList();
            // 删除所有关联子件
            quoteDetailItemService.deleteByDetailIds(oldDetailIds);
            // 删除父行
            quoteDetailService.removeBatchByIds(oldDetailIds);
        }
    }



    /**
     * 通用校验与计算包装器
     * @param dto 数据对象
     * @param calculator 计算函数 (策略模式)
     * @param resultList 结果集合
     */
    private <T> String validateAndCalc(T dto, Function<T, QuoteDetail> calculator, List<QuoteDetail> resultList,Integer projectType) {
        StringBuilder sb = new StringBuilder();
        // JSR 303 校验
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        for (ConstraintViolation<T> v : violations) {
            sb.append(v.getMessage()).append("; ");
        }

        // 如果基础校验通过，尝试执行业务计算
        if (sb.isEmpty()) {
            try {
                QuoteDetail detail = calculator.apply(dto); // 调用 calculateService
                detail.setProjectType(projectType);//赋值项目类型
                resultList.add(detail);
            } catch (Exception e) {
                sb.append("计算错误:").append(e.getMessage());
            }
        }
        return !sb.isEmpty() ? sb.toString() : null;
    }

    /**
     * 导出多Sheet错误报告
     */
    private void handleMultiSheetErrorExport(List<QuoteDetailFolderImportExcelDto> foldings, List<QuoteDetailProductImportExcelDto> products) {
        String fileName = "导入失败报告_" + System.currentTimeMillis() + ".xlsx";
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ExcelWriter excelWriter = EasyExcel.write(out).build()) {

            // 写入 Sheet1 (需要定义含 errorMsg 的 DTO，或者利用 EasyExcel 动态追加列，这里假设 DTO 已包含 errorMsg)
            WriteSheet sheet1 = EasyExcel.writerSheet(0, "折件").head(QuoteDetailFolderImportExcelDto.class).build();
            excelWriter.write(foldings, sheet1);

            // 写入 Sheet2
            WriteSheet sheet2 = EasyExcel.writerSheet(1, "制品").head(QuoteDetailProductImportExcelDto.class).build();
            excelWriter.write(products, sheet2);

            // 务必 finish，否则流不完整
            excelWriter.finish();

            // 上传并抛出异常
            String url = sysFileService.upload(out.toByteArray(), fileName);
            throw new ImportValidationException(url, "导入数据有误，请下载报告");

        } catch (IOException e) {
            throw new ServiceException("生成错误报告失败");
        }
    }

}
