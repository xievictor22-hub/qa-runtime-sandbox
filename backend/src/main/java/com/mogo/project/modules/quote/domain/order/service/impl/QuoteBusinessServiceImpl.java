package com.mogo.project.modules.quote.domain.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteBusinessItemMapper;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteDetailMapper;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteOrderMapper;
import com.mogo.project.modules.quote.domain.order.service.component.QuoteBusinessPriceCalculator;
import com.mogo.project.modules.quote.domain.order.entity.QuoteBusinessItem;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.order.entity.QuoteOrder;
import com.mogo.project.modules.quote.domain.order.service.IQuoteBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
        List<QuoteBusinessItem> businessItems = costDetails.stream().map(detail -> {
            QuoteBusinessItem item = new QuoteBusinessItem();
            item.setQuoteId(quoteId);
            item.setDetailId(detail.getId()); // 关联原始明细ID
            item.setBusinessVersion(version);         // 设置业务版本 (例如 1)

//            // --- 价格初始化逻辑 ---
//            // 初始时：原价 = 出厂总价
//            item.setOriginalTotal(detail.getFactoryTotal());
//            // 初始时：折扣率 = 100% (不打折)
//            item.setDiscountRate(new BigDecimal("100"));
//            // 初始时：折扣后价格 = 原价
//            item.setDiscountTotal(detail.getFactoryTotal());
//            // 初始时：最终成交价 = 原价
//            item.setFinalTotal(detail.getFactoryTotal());

            item.setLockStatus(false); // 初始未锁，允许业务员编辑
            return item;
        }).collect(Collectors.toList());

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

        List<Long> ids = items.stream()
                .map(QuoteBusinessItem::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            throw new ServiceException("业务明细缺少ID");
        }

        List<QuoteBusinessItem> dbItems = this.listByIds(ids);
        if (dbItems.size() != ids.size()) {
            throw new ServiceException("存在无效的业务明细ID");
        }

        Set<Long> quoteIds = dbItems.stream().map(QuoteBusinessItem::getQuoteId).collect(Collectors.toSet());
        Set<Integer> versions = dbItems.stream().map(QuoteBusinessItem::getBusinessVersion).collect(Collectors.toSet());
        if (quoteIds.size() != 1 || versions.size() != 1) {
            throw new ServiceException("仅允许同一报价单、同一业务版本批量保存");
        }

        Map<Long, QuoteBusinessItem> submitMap = items.stream()
                .filter(item -> item.getId() != null)
                .collect(Collectors.toMap(QuoteBusinessItem::getId, item -> item, (a, b) -> b));

        List<Long> detailIds = dbItems.stream()
                .map(QuoteBusinessItem::getDetailId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, QuoteDetail> detailMap = quoteDetailMapper.selectBatchIds(detailIds).stream()
                .collect(Collectors.toMap(QuoteDetail::getId, d -> d, (a, b) -> a));

        List<QuoteBusinessItem> updateList = new ArrayList<>();
        for (QuoteBusinessItem dbItem : dbItems) {
            QuoteBusinessItem submitItem = submitMap.get(dbItem.getId());
            if (submitItem == null) {
                continue;
            }

            QuoteBusinessItem before = copyForCompare(dbItem);
            applyEditableFields(dbItem, submitItem);
            normalizeRates(dbItem);

            BigDecimal quantity = detailMap.get(dbItem.getDetailId()) == null
                    ? BigDecimal.ZERO
                    : detailMap.get(dbItem.getDetailId()).getQuantity();

            quoteBusinessPriceCalculator.recalculate(dbItem, quantity);
            if (isChanged(before, dbItem)) {
                updateList.add(dbItem);
            }
        }

        if (!updateList.isEmpty()) {
            this.updateBatchById(updateList);
        }
    }

    private QuoteBusinessItem copyForCompare(QuoteBusinessItem src) {
        QuoteBusinessItem copy = new QuoteBusinessItem();
        copy.setFactoryProfitRate(src.getFactoryProfitRate());
        copy.setCustomerFactoryTotal(src.getCustomerFactoryTotal());
        copy.setInstallProfitRate(src.getInstallProfitRate());
        copy.setCustomerInstallTotal(src.getCustomerInstallTotal());
        copy.setSalesPoint(src.getSalesPoint());
        copy.setCustomerUnitPrice(src.getCustomerUnitPrice());
        copy.setRemark(src.getRemark());
        return copy;
    }

    private boolean isChanged(QuoteBusinessItem before, QuoteBusinessItem after) {
        return !Objects.equals(before.getFactoryProfitRate(), after.getFactoryProfitRate())
                || !Objects.equals(before.getCustomerFactoryTotal(), after.getCustomerFactoryTotal())
                || !Objects.equals(before.getInstallProfitRate(), after.getInstallProfitRate())
                || !Objects.equals(before.getCustomerInstallTotal(), after.getCustomerInstallTotal())
                || !Objects.equals(before.getSalesPoint(), after.getSalesPoint())
                || !Objects.equals(before.getCustomerUnitPrice(), after.getCustomerUnitPrice())
                || !Objects.equals(before.getRemark(), after.getRemark());
    }

    private void applyEditableFields(QuoteBusinessItem target, QuoteBusinessItem submit) {
        target.setFactoryProfitRate(submit.getFactoryProfitRate());
        target.setCustomerFactoryTotal(submit.getCustomerFactoryTotal());
        target.setInstallProfitRate(submit.getInstallProfitRate());
        target.setCustomerInstallTotal(submit.getCustomerInstallTotal());
        target.setSalesPoint(submit.getSalesPoint());
        target.setCustomerUnitPrice(submit.getCustomerUnitPrice());
        target.setRemark(submit.getRemark());
    }

    /**
     * 前端以“百分数口径”(10=10%)传值，后端统一转为“倍率口径”(0.1=10%)
     */
    private void normalizeRates(QuoteBusinessItem item) {
        item.setFactoryProfitRate(normalizePercentField(item.getFactoryProfitRate()));
        item.setInstallProfitRate(normalizePercentField(item.getInstallProfitRate()));
        item.setSalesPoint(normalizePercentField(item.getSalesPoint()));
    }

    private BigDecimal normalizePercentField(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value.abs().compareTo(BigDecimal.ONE) > 0) {
            return value.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        }
        return value;
    }
}
