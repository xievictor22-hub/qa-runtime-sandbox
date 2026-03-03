package com.mogo.project.modules.quote.domain.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import com.mogo.project.modules.quote.domain.order.vo.QuoteOrderVo;


/**1. 基础管理服务 (IQuoteManageService)
负责：CRUD、列表查询、详情、导入导出。*/
public interface IQuoteManageService extends IService<QuoteOrder> {
    Boolean createQuote(QuoteOrder quote);
    IPage<QuoteOrder> selectTodoList(Page<QuoteOrder> page, QuoteOrder query);
    IPage<QuoteOrder> selectOrderPage(Page<QuoteOrder> page, QuoteOrder query);
    QuoteOrder getByIdSafe(Long id);
    QuoteOrderVo getDetailWithPerms(Long id);//带操作权限的详情
}