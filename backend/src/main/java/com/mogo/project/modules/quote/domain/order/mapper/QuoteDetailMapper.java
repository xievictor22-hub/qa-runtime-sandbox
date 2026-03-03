package com.mogo.project.modules.quote.domain.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuoteDetailMapper extends BaseMapper<QuoteDetail> {
}