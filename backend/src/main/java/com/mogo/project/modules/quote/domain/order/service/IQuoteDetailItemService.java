package com.mogo.project.modules.quote.domain.order.service;

import com.baomidou.mybatisplus.extension.service.IService;

import com.mogo.project.modules.quote.domain.order.entity.QuoteDetailItem;
import com.mogo.project.modules.quote.domain.order.vo.QuoteDetailItemVo;

import java.util.List;

/**
 * 报价组件明细
 */
public interface IQuoteDetailItemService extends IService<QuoteDetailItem> {

    /**
     * 根据父行ID查询所有子件
     */
    List<QuoteDetailItemVo> listByDetailId(Long detailId);

    /**
     * 添加子件，并自动更新父行总价
     */
    void addItem(QuoteDetailItem quoteDetailItem);

    /**
     * 修改子件，并自动更新父行总价
     */
    void updateItem(QuoteDetailItem item);

    /**
     * 删除子件，并自动更新父行总价
     */
    void deleteItem(Long id);

    Long countByVersion(String version);

    void deleteByDetailIds(List<Long> parentIds);
}