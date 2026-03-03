package com.mogo.project.modules.quote.domain.basePrice.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteFoldingRule;
import com.mogo.project.modules.quote.domain.basePrice.service.IQuoteFoldingRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class QuoteRuleCacheManager {

    private final IQuoteFoldingRuleService quoteFoldingRuleService;

    // 缓存容器：Key = 版本号, Value = 该版本下的所有规则
    // 使用 ConcurrentHashMap 保证线程安全
    private final Map<String, List<QuoteFoldingRule>> RULE_CACHE = new ConcurrentHashMap<>();

    public List<QuoteFoldingRule> getRulesByVersion(String version,Integer projectType) {
        if(RULE_CACHE.containsKey(version)) {
            return RULE_CACHE.get(version);
        }
        log.info("加载版本 {}的底价规则导内存...", version);
        List<QuoteFoldingRule> rules = quoteFoldingRuleService.list(new LambdaQueryWrapper<QuoteFoldingRule>()
                .eq(QuoteFoldingRule::getVersion, version)
        .eq(QuoteFoldingRule::getProjectType, projectType));
        RULE_CACHE.put(version, rules);
        return rules;
    }

    public void  refreshVersion(String version) {
        RULE_CACHE.remove(version);
        log.info("版本{}的缓存已被删除", version);
    }

}
