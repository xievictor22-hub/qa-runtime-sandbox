package com.mogo.project.modules.quote.domain.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mogo.project.modules.quote.domain.order.entity.QuoteBusinessItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuoteBusinessItemMapper extends BaseMapper<QuoteBusinessItem> {
    /**
     * 查询业务调整明细（关联 Detail 表获取产品名）
     * @param quoteId 报价单ID
     * @param version 业务版本号
     */
    List<QuoteBusinessItem> selectBusinessList(@Param("quoteId") Long quoteId, @Param("version") Integer version);
}