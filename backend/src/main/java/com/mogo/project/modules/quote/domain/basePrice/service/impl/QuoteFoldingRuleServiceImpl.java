package com.mogo.project.modules.quote.domain.basePrice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteFoldingRule;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteImportSource;
import com.mogo.project.modules.quote.domain.basePrice.mapper.QuoteFoldingRuleMapper;
import com.mogo.project.modules.quote.domain.basePrice.mapper.QuoteImportSourceMapper;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteFoldingRuleService;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteImportSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 业务调整价格模块
 */
@Service
@RequiredArgsConstructor
public class QuoteFoldingRuleServiceImpl extends ServiceImpl<QuoteFoldingRuleMapper, QuoteFoldingRule> implements IQuoteFoldingRuleService {


    /**
     * 根据版本删除
     * @param version
     */
    @Override
    public void deleteByVersion(String version) {
        baseMapper.delete(new LambdaQueryWrapper<QuoteFoldingRule>().eq(QuoteFoldingRule::getVersion,version));
    }

    @Override
    public void deleteByVersionPhysically(String version) {
        baseMapper.deleteByVersionPhysically(version);
    }
}