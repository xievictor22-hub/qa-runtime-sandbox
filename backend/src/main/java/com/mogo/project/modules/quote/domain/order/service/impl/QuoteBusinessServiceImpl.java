package com.mogo.project.modules.quote.domain.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteBusinessItemMapper;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteDetailMapper;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteOrderMapper;
import com.mogo.project.modules.quote.domain.order.entity.QuoteBusinessItem;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import com.mogo.project.modules.quote.domain.order.service.IQuoteBusinessService;
import com.mogo.project.modules.quote.domain.order.service.component.QuoteBusinessPriceCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 业务调整价格模块
 */
@Service
@RequiredArgsConstructor
public class QuoteBusinessServiceImpl extends ServiceImpl<QuoteBusinessItemMapper, QuoteBusinessItem> implements IQuoteBusinessService {

    private final QuoteOrderMapper quoteOrderMapper;
    private final QuoteDetailMapper quoteDetailMapper;
    private final QuoteBusinessPriceCalculator quoteBusinessPriceCalculator;

    /**
     *查询当前版本的报价单的业务报价信息
     * @param quoteId
     * @return
     */
    @Override
    public List<QuoteBusinessItem> listCurrentVersion(Long quoteId) {
        // 1. 查主表获取当前业务版本号
        QuoteOrder order = quoteOrderMapper.selectById(quoteId);

        // 如果还没有生成过业务数据(版本为0或null)，直接返回空
        if (order == null || order.getCurrentBusinessVersion() == null || order.getCurrentBusinessVersion() == 0) {
            return Collections.emptyList();
        }

        // 2. 调用自定义 Mapper 进行连表查询
        return baseMapper.selectBusinessList(quoteId, order.getCurrentBusinessVersion());
    }

    /**
     * 复制并创建新版本业务标价明细
     * @param quoteId
     * @param version
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initBusinessItems(Long quoteId, Integer version) {
        // 1. 获取报价单主表信息
        QuoteOrder order = quoteOrderMapper.selectById(quoteId);
        if (order == null) throw new ServiceException("报价单不存在");

        // 2. 获取当前锁定的报价明细 (QuoteDetail)
        // 这里的 CurrentQuoteVersion 是报价员最终确定的版本
        Integer costVersion = order.getCurrentQuoteVersion();
        List<QuoteDetail> costDetails = quoteDetailMapper.selectList(new LambdaQueryWrapper<QuoteDetail>()
                .eq(QuoteDetail::getQuoteId, quoteId)
                .eq(QuoteDetail::getDetailVersion, costVersion));

        if (costDetails.isEmpty()) {
            return; // 无明细数据，直接返回
        }

        // 3. 转换为业务明细 (QuoteBusinessItem)
        List<QuoteBusinessItem> businessItems = costDetails.stream()
                .map(detail -> quoteBusinessPriceCalculator.initFromDetail(quoteId, version, detail))
                .collect(Collectors.toList());

        // 4. 批量保存
        this.saveBatch(businessItems);
    }

    /**
     * 更新业务报价信息
     * @param items
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateBusinessItems(List<QuoteBusinessItem> items) {
        if (items == null || items.isEmpty()) return;
        for (QuoteBusinessItem item : items) {
            if (Boolean.TRUE.equals(item.getLockStatus())) {
                throw new ServiceException("已锁定数据不允许修改");
            }
            quoteBusinessPriceCalculator.recalculate(item, null);
        }
        this.updateBatchById(items);
    }
}