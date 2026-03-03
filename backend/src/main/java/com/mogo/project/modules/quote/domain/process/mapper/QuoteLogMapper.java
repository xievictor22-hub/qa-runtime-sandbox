package com.mogo.project.modules.quote.domain.process.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mogo.project.modules.quote.domain.process.entity.QuoteLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuoteLogMapper extends BaseMapper<QuoteLog> {
    // 添加方法定义
    List<QuoteLog> selectLogListByQuoteId(@Param("quoteId") Long quoteId);
}