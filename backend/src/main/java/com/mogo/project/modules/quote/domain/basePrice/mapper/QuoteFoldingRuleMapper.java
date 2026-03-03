package com.mogo.project.modules.quote.domain.basePrice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mogo.project.modules.quote.domain.basePrice.entity.QuoteFoldingRule;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuoteFoldingRuleMapper  extends BaseMapper<QuoteFoldingRule> {

    @Delete("DELETE from quote_folding_rule where version = #{version} ")
    void deleteByVersionPhysically(String version);
}
