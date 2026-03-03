package com.mogo.project.modules.quote.domain.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.quote.domain.order.entity.QuoteBusinessItem;

import java.util.List;

/**
 * 业务调整价格服务
 */
public interface IQuoteBusinessService extends IService<QuoteBusinessItem> {
    /** 获取当前业务版本的明细 */
    List<QuoteBusinessItem> listCurrentVersion(Long quoteId);

    /**
     * 初始化业务明细数据
     * (通常在审核通过后调用，基于最新的报价明细生成 v1 业务数据)
     * @param quoteId 报价单ID
     * @param version 初始化的业务版本号 (通常是1)
     */
    void initBusinessItems(Long quoteId, Integer version);

    /** 批量保存 (带校验) */
    void batchUpdateBusinessItems(List<QuoteBusinessItem> items);
}
