package com.mogo.project.modules.quote.domain.order.service.component;


import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteFoldingRule;
import com.mogo.project.modules.quote.domain.basePrice.manager.QuoteRuleCacheManager;
import com.mogo.project.modules.quote.domain.order.constant.QuoteMatchConstant;
import com.mogo.project.modules.quote.domain.order.convert.QuoteDetailFolderImportExcelDtoConvert;
import com.mogo.project.modules.quote.domain.order.convert.QuoteDetailProductImportExcelDtoConvert;
import com.mogo.project.modules.quote.domain.order.dto.QuoteDetailFolderImportExcelDto;
import com.mogo.project.modules.quote.domain.order.dto.QuoteDetailProductImportExcelDto;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;




@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteCalculationService {

    private final QuoteDetailFolderImportExcelDtoConvert quoteDetailFolderImportExcelDtoConvert;
    private final QuoteDetailProductImportExcelDtoConvert quoteDetailProductImportExcelDtoConvert;
    private final QuoteRuleCacheManager quoteRuleCacheManager;
    private final FormulaCalculator formulaCalculator;


    /**
     * 复用逻辑：计算折件 (DTO -> Entity)
     * 适用于：导入、单条新增
     * @param dto 导入表数据
     * @param quoteId 报价单id
     * @param version  当前报价员报价版本
     * @return
     */
    public QuoteDetail calculateFolding(QuoteDetailFolderImportExcelDto dto, Long quoteId, Integer version,Integer projectType,Integer addNum) throws ServiceException {
        //基础数据转化
        QuoteDetail entity = quoteDetailFolderImportExcelDtoConvert.toEntity(dto);
        entity.setDetailVersion(version);//设置报价版本
        entity.setQuoteId(quoteId);
        entity.setDetailType(0);
        entity.setRowNum(entity.getRowNum()+addNum);
        //预计算核心参数
        BigDecimal w = formulaCalculator.calculate(dto.getSpec());//公式取宽度  1+2+3
        BigDecimal c = this.getCutBySpecSafely(dto.getSpec());//公式取刀数 1+2+3
        BigDecimal quantity = entity.getQuantity();//数量
        BigDecimal d = entity.getThickness();//板厚
        BigDecimal h = entity.getLength();//长
        String sourceVerCode = entity.getVersion();//底价库报价版本号

        //回填计算属性
        entity.setWidth(w);

        // 🚀 性能优化关键点：一次性获取该版本所有规则，传入后续方法复用
        List<QuoteFoldingRule> allRules = quoteRuleCacheManager.getRulesByVersion(sourceVerCode,projectType);

        //计算材料价格，导入时已合并板材材料与制作材料
        BigDecimal materialPrice = calculateSingleItem(allRules
                , sourceVerCode, QuoteMatchConstant.PROCESS_MATERIAL, entity.getMaterial(), h, w, d, null, null);
        entity.setMaterialPrice(materialPrice);
        // 3. 剪折价格
        BigDecimal foldingPrice = BigDecimal.ZERO;
        if (isValidOption(entity.getFolding())) {
            foldingPrice = calculateSingleItem(allRules, sourceVerCode,
                    QuoteMatchConstant.PROCESS_FOLDING, entity.getFolding(),
                    h, w, null, w, c); // 剪折看宽度和刀数
        }
        entity.setFoldingPrice(foldingPrice);

        // 4. 开槽价格
        BigDecimal slottingPrice = BigDecimal.ZERO;
        if (isValidOption(entity.getSlotting())) {
            slottingPrice = calculateSingleItem(allRules, sourceVerCode,
                    QuoteMatchConstant.PROCESS_FOLDING, QuoteMatchConstant.OPTION_SLOTTING,
                    h, w, d, null, null);
        }
        entity.setSlottingPrice(slottingPrice);

        // 5. 安装价格
        BigDecimal installPrice = BigDecimal.ZERO;
        if (isValidOption(entity.getInstall())) {
            // 安装通常匹配二级分类
            installPrice = calculateSingleItem(allRules, sourceVerCode,
                    QuoteMatchConstant.PROCESS_INSTALL, entity.getCategoryLvl2(),
                    h, w, null, w, null);
        }
        entity.setInstallPrice(installPrice);

        // 6. 无指纹
        BigDecimal noFingerPrice = BigDecimal.ZERO;
        if (isValidOption(entity.getNoFingerprint())) {
            noFingerPrice = calculateSingleItem(allRules, sourceVerCode,
                    QuoteMatchConstant.PROCESS_SURFACE, QuoteMatchConstant.OPTION_NO_FINGERPRINT,
                    h, w, d, null, null);
        }
        entity.setNoFingerprintPrice(noFingerPrice);

        // 7. 颜色
        BigDecimal colorPrice = BigDecimal.ZERO;
        String color = entity.getColor();
        if (isValidOption(color) && !QuoteMatchConstant.OPTION_ORIGINAL_COLOR.equals(color)) {
            colorPrice = calculateSingleItem(allRules, sourceVerCode,
                    QuoteMatchConstant.PROCESS_SURFACE, color,
                    h, w, d, null, null);
        }
        entity.setColorPrice(colorPrice);
        //8. 纹路价格
        BigDecimal texturePrice = BigDecimal.ZERO;
        String texture = entity.getTexture();
        if(isValidOption(texture)) {
             texturePrice = calculateSingleItem(allRules, sourceVerCode
                    , QuoteMatchConstant.PROCESS_SURFACE, texture
                    , h, w, d, null, null);
            entity.setTexturePrice(texturePrice);
        }
        //激光价格todo 暂时不算
        entity.setLaserMPrice(BigDecimal.ZERO);


        //计算生产成本单价
        BigDecimal factoryCostUnitPrice = entity.getMaterialPrice().add(entity.getFoldingPrice()).add(entity.getSlottingPrice())
                .add(entity.getNoFingerprintPrice()).add(entity.getColorPrice())
                .add(entity.getTexturePrice()).add(entity.getLaserMPrice()).setScale(2, RoundingMode.HALF_UP);
        //安装成本单价
        //出厂单价  生产成本单价+安装成本单价
        BigDecimal distPrice = factoryCostUnitPrice.add(installPrice).setScale(2, RoundingMode.HALF_UP);
        //只给成本单价
        entity.setDistPrice(distPrice);
        entity.setFactoryCostUnitPrice(factoryCostUnitPrice);
        entity.setInstallCostUnitPrice(installPrice);
        //出厂总价
        BigDecimal setSummaryPrice = distPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
        entity.setSummaryPrice(setSummaryPrice);
        return entity;
    }


    /**
     * 复用逻辑：计算制品 (DTO -> Entity)
     */
    public QuoteDetail calculateProduct(QuoteDetailProductImportExcelDto dto, Long quoteId, Integer version,Integer projectType) {
        QuoteDetail entity = quoteDetailProductImportExcelDtoConvert.toEntity(dto);
        entity.setQuoteId(quoteId);
        entity.setDetailVersion(version);
        entity.setDetailType(1);
        entity.setProjectType(projectType);
        return entity;
    }


    /**
     * 通用单项价格计算方法
     * 作用：封装 查表 -> 判空 -> 算公式 -> 乘单价 的全过程
     */
    private BigDecimal calculateSingleItem(List<QuoteFoldingRule> rules, String version,
                                           String process1, String process2,
                                           BigDecimal h, BigDecimal w,
                                           BigDecimal matchD, BigDecimal matchW, BigDecimal matchC) {

        // 1. 内存匹配
        QuoteFoldingRule rule = rules.stream()
                .filter(r -> isMatch(r, process1, process2, matchW, matchD, matchC))
                .findFirst()
                .orElseThrow(() -> new ServiceException(
                        String.format("未找到底价规则！版本:%s, 工艺:[%s-%s], 宽:%s, 厚:%s, 刀:%s",
                                version, process1, process2, matchW, matchD, matchC)));

        // 2. 公式计算 (数量/面积/长度)
        // 注意：这里的 w 和 h 是用于公式计算的变量，而 matchW 是用于查找规则的变量，不要混淆
        BigDecimal quantityFactor = formulaCalculator.calculate(rule.getQuoteFormula(), h, w);

        // 3. 计算价格
        return quantityFactor.multiply(rule.getUnitPrice());
    }

  /**匹配单价*/
  private boolean isMatch(QuoteFoldingRule r, String process1, String process2, BigDecimal w, BigDecimal d, BigDecimal c) {
      // 1. 字符串匹配
      if (process1 != null && !process1.equals(r.getProcess1())) return false;
      if (process2 != null && !process2.equals(r.getProcess2())) return false;

      // 2. 数值区间匹配 (使用提取的工具方法)
      // 如果 w 不为空，但区间不匹配，返回 false
      if (w != null && !checkRange(w, r.getWMin(), r.getWMinInclude(), r.getWMax(), r.getWMaxInclude())) {
          return false;
      }

      if (d != null && !checkRange(d, r.getDMin(), r.getDMinInclude(), r.getDMax(), r.getDMaxInclude())) {
          return false;
      }

      if (c != null && !checkRange(c, r.getCMin(), r.getCMinInclude(), r.getCMax(), r.getCMaxInclude())) {
          return false;
      }

      return true;
  }
    /**
     * 通用区间检查工具
     * 核心逻辑：规则值为 NULL 代表不限制
     */
    private boolean checkRange(BigDecimal inputVal, BigDecimal ruleMin, Integer minInclude, BigDecimal ruleMax, Integer maxInclude) {
        // 1. 检查下限 (Min)
        if (ruleMin != null) {
            int cmp = inputVal.compareTo(ruleMin);
            // 如果包含(>=)，cmp必须>=0；如果不包含(>)，cmp必须>0
            boolean minPass = (minInclude != null && minInclude == 1) ? (cmp >= 0) : (cmp > 0);
            if (!minPass) return false;
        }

        // 2. 检查上限 (Max)
        if (ruleMax != null) {
            int cmp = inputVal.compareTo(ruleMax);
            // 如果包含(<=)，cmp必须<=0；如果不包含(<)，cmp必须<0
            boolean maxPass = (maxInclude != null && maxInclude == 1) ? (cmp <= 0) : (cmp < 0);
            if (!maxPass) return false;
        }

        // 如果 min 和 max 都是 null，或者都在范围内，返回 true
        return true;
    }

    /**
     * 简单的选项校验
     */
    private boolean isValidOption(String value) {
        return value != null && !value.isEmpty() && !QuoteMatchConstant.OPTION_NO.equals(value);
    }

    /**
     * 更安全的刀数计算
     */
    private BigDecimal getCutBySpecSafely(String spec) {
        if (spec == null || spec.isEmpty()) {
            return BigDecimal.ZERO;
        }
        // 逻辑：以 "+" 分割，段数 - 1 即为刀数 (仅适用于 A+B+C 格式)
        // 如果是 100 这种没有加号的，刀数为 0
        String[] parts = spec.split("\\+");
        return new BigDecimal(Math.max(0, parts.length - 1));
    }
}
