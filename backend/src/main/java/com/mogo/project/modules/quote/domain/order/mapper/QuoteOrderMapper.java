package com.mogo.project.modules.quote.domain.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuoteOrderMapper extends BaseMapper<QuoteOrder> {


    IPage<QuoteOrder> selectOrderList(Page<QuoteOrder> page, @Param("query") QuoteOrder query);
    // 暂时不需要自定义 SQL，MyBatis Plus 已提供 CRUD
}