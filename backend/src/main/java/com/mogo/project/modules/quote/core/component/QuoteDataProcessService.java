package com.mogo.project.modules.quote.core.component;


import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.common.util.RangeUtil;
import com.mogo.project.modules.quote.domain.basePrice.mapper.QuoteFoldingRuleMapper;
import com.mogo.project.modules.quote.domain.basePrice.mapper.QuoteProductLibraryMapper;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteFoldingRule;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteImportSource;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteProductLibrary;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteFoldingRuleService;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteProductLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理报价底表，转成折件报价表与制品报价表
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QuoteDataProcessService {

    private final IQuoteProductLibraryService quoteProductLibraryService;
    private final IQuoteFoldingRuleService quoteFoldingRuleService;

    @Transactional(rollbackFor = Exception.class)
    public void processAndDistribute(List<QuoteImportSource> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) return;

        List<QuoteProductLibrary> products = new ArrayList<>();
        List<QuoteFoldingRule> foldingRules = new ArrayList<>();

        for (QuoteImportSource src : sourceList) {
            try {
            // 1. 处理【制品底价库】
            QuoteProductLibrary product = new QuoteProductLibrary();
            BeanUtils.copyProperties(src, product);
            product.setJudgeVal(src.getRangeValC());
            product.setWVal(src.getRangeValW());
            product.setDVal(src.getRangeValD());
            products.add(product);

            // 2. 处理【折件底价规则】
            if (src.getIsFolding() != null && src.getIsFolding() != 0) {
                QuoteFoldingRule rule = new QuoteFoldingRule();
                BeanUtils.copyProperties(src, rule, "id", "quoteFormula");
                //对板材、制作材料进行合并
                rule.setProcess1(StringUtils.contains( rule.getProcess1(),"材料")?"材料":rule.getProcess1());
                // 2.1 动态映射 Process2
                switch (src.getIsFolding()) {
                    case 2: rule.setProcess2(src.getProcess2()); break;
                    case 3: rule.setProcess2(src.getProcess3()); break;
                    case 4: rule.setProcess2(src.getProcess4()); break;
                    default: rule.setProcess2(src.getProcess2());
                }
                //去掉-2b
                if(StringUtils.contains( rule.getProcess2(),"-2b"))rule.setProcess2(StringUtils.replace(rule.getProcess2(),"-2b",""));
                // 2.2 范围解析 (调用 RangeUtil)
                mapRanges(src, rule);

                // 2.3 拼接公式字符串
                String formulaStr = buildFormula(src.getUnit(), src.getQuoteFormula());
                rule.setQuoteFormula(formulaStr);
                foldingRules.add(rule);
            }
            } catch (Exception e) {
                // 这里是最后一道防线：如果在分发过程还能报错，说明有代码逻辑 bug 或脏数据
                log.error("数据分发转换异常，ID: {}, 内容: {}", src.getId(), src, e);
                // 此时抛出异常会回滚整个事务
                throw new ServiceException("数据转换分发失败，请联系管理员查看日志");
            }
        }

        // 批量保存 (假设 mapper 有 insertBatch XML 实现，或循环调用 insert)
        quoteProductLibraryService.saveBatch(products);
        quoteFoldingRuleService.saveBatch(foldingRules);
    }

    private void mapRanges(QuoteImportSource src, QuoteFoldingRule rule) {
        var cRes = RangeUtil.parse(src.getRangeValC());
        if (cRes.isValid()) {
            rule.setCMin(cRes.getMin()); rule.setCMax(cRes.getMax());
            rule.setCMinInclude(cRes.getMinInclude()); rule.setCMaxInclude(cRes.getMaxInclude());
        }
        var wRes = RangeUtil.parse(src.getRangeValW());
        if (wRes.isValid()) {
            rule.setWMin(wRes.getMin()); rule.setWMax(wRes.getMax());
            rule.setWMinInclude(wRes.getMinInclude()); rule.setWMaxInclude(wRes.getMaxInclude());
        }
        var dRes = RangeUtil.parse(src.getRangeValD());
        if (dRes.isValid()) {
            rule.setDMin(dRes.getMin()); rule.setDMax(dRes.getMax());
            rule.setDMinInclude(dRes.getMinInclude()); rule.setDMaxInclude(dRes.getMaxInclude());
        }
    }

    private String buildFormula(String unit, BigDecimal coef) {
        BigDecimal i = (coef != null) ? coef : BigDecimal.ONE;
        String u = (unit == null) ? "" : unit.trim().toLowerCase();
        if (u.equals("㎡") || u.equals("m2")) return "H*W/1000000*" + i;
        if (u.equals("m")) return "H/1000*" + i;
        return i.toString();
    }

}