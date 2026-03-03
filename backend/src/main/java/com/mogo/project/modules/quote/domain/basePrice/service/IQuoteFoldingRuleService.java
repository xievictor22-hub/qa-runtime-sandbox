package com.mogo.project.modules.quote.domain.basePrice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteFoldingRule;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteProductLibrary;


public interface IQuoteFoldingRuleService extends IService<QuoteFoldingRule> {

    void deleteByVersion(String version);

    void deleteByVersionPhysically(String version);


}
