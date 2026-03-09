package com.mogo.project.modules.quote.domain.order.service.component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mogo.project.modules.quote.domain.order.entity.QuoteBusinessItem;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteBusinessItemMapper;
import com.mogo.project.modules.quote.domain.order.mapper.QuoteDetailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务版本初始化器
 * 对外按场景提供独立入口：
 * 1) 初次审核通过进入业务
 * 2) 已完成后重开业务(复制业务版本)
 * 3) 业务回推到报价后，再审核通过进入业务
 */
@Component
@RequiredArgsConstructor
public class BusinessVersionInitializer {

    private final QuoteBusinessItemMapper quoteBusinessMapper;
    private final QuoteDetailMapper quoteDetailMapper;
    private final QuoteBusinessPriceCalculator quoteBusinessPriceCalculator;

    /** 场景1：初次进入业务，全部按默认业务值初始化 */
    public List<QuoteBusinessItem> initFirstBusinessVersion(Long quoteId,
                                                            List<QuoteDetail> quoteDetails,
                                                            Integer newBusinessVersion) {
        return safeQuoteDetails(quoteDetails).stream()
                .filter(detail -> !isDeletedRow(detail))
                .map(detail -> initBusinessItem(quoteId, newBusinessVersion, detail, null))
                .collect(Collectors.toList());
    }

    /** 场景2：已完成后重开业务，复制上一业务版本数据到新版本 */
    public List<QuoteBusinessItem> cloneForReAdjustBusiness(Long quoteId,
                                                            Integer oldBusinessVersion,
                                                            Integer newBusinessVersion) {
        List<QuoteBusinessItem> oldItems = quoteBusinessMapper.selectList(new LambdaQueryWrapper<QuoteBusinessItem>()
                .eq(QuoteBusinessItem::getQuoteId, quoteId)
                .eq(QuoteBusinessItem::getBusinessVersion, oldBusinessVersion));

        if (oldItems == null || oldItems.isEmpty()) {
            return Collections.emptyList();
        }

        return oldItems.stream().map(item -> {
            QuoteBusinessItem newItem = new QuoteBusinessItem();
            BeanUtils.copyProperties(item, newItem, "id", "createTime", "updateTime");
            newItem.setBusinessVersion(newBusinessVersion);
            newItem.setLockStatus(false);
            return newItem;
        }).collect(Collectors.toList());
    }

    /**
     * 场景3：业务回推到报价后，再审核通过进入业务
     * - 有旧业务行且未标记新增/变更：继承旧业务值
     * - 标记为新增/变更，或旧行不存在：按默认业务值初始化
     */
    public List<QuoteBusinessItem> initFromReQuotedVersion(Long quoteId,
                                                           List<QuoteDetail> newQuoteDetails,
                                                           Integer oldBusinessVersion,
                                                           Integer newBusinessVersion) {
        List<QuoteDetail> normalizedDetails = safeQuoteDetails(newQuoteDetails);
        if (normalizedDetails.isEmpty()) {
            return Collections.emptyList();
        }

        List<QuoteBusinessItem> oldBusinessItems = quoteBusinessMapper.selectList(new LambdaQueryWrapper<QuoteBusinessItem>()
                .eq(QuoteBusinessItem::getQuoteId, quoteId)
                .eq(QuoteBusinessItem::getBusinessVersion, oldBusinessVersion));

        if (oldBusinessItems == null || oldBusinessItems.isEmpty()) {
            return initFirstBusinessVersion(quoteId, normalizedDetails, newBusinessVersion);
        }

        Map<String, OldBusinessContext> oldBusinessByKey = buildOldBusinessContextMap(oldBusinessItems);

        List<QuoteBusinessItem> result = new ArrayList<>();
        for (QuoteDetail newDetail : normalizedDetails) {
            if (isDeletedRow(newDetail)) {
                continue;
            }

            OldBusinessContext context = oldBusinessByKey.get(buildDetailIdentityKey(newDetail));
            QuoteBusinessItem oldItem = context == null ? null : context.item;
            QuoteDetail oldDetail = context == null ? null : context.detail;

            if (isMarkedAsNewByVersion(newDetail, oldDetail)) {
                oldItem = null;
            }

            result.add(initBusinessItem(quoteId, newBusinessVersion, newDetail, oldItem));
        }
        return result;
    }

    private List<QuoteDetail> safeQuoteDetails(List<QuoteDetail> quoteDetails) {
        return quoteDetails == null ? Collections.emptyList() : quoteDetails;
    }

    private Map<String, OldBusinessContext> buildOldBusinessContextMap(List<QuoteBusinessItem> oldBusinessItems) {
        Map<Long, QuoteDetail> oldDetailById = quoteDetailMapper.selectBatchIds(oldBusinessItems.stream()
                        .map(QuoteBusinessItem::getDetailId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(QuoteDetail::getId, Function.identity(), (a, b) -> a));

        Map<String, OldBusinessContext> oldBusinessByKey = new HashMap<>();
        for (QuoteBusinessItem oldItem : oldBusinessItems) {
            QuoteDetail oldDetail = oldDetailById.get(oldItem.getDetailId());
            if (oldDetail == null) {
                continue;
            }
            oldBusinessByKey.put(buildDetailIdentityKey(oldDetail), new OldBusinessContext(oldItem, oldDetail));
        }
        return oldBusinessByKey;
    }

    private QuoteBusinessItem initBusinessItem(Long quoteId, Integer businessVersion, QuoteDetail quoteDetail, QuoteBusinessItem oldItem) {
        if (oldItem == null) {
            return quoteBusinessPriceCalculator.initFromDetail(quoteId, businessVersion, quoteDetail);
        }

        QuoteBusinessItem item = new QuoteBusinessItem();
        BeanUtils.copyProperties(oldItem, item, "id", "createTime", "updateTime", "createBy", "updateBy", "lockVersion");
        item.setQuoteId(quoteId);
        item.setDetailId(quoteDetail.getId());
        item.setBusinessVersion(businessVersion);
        item.setLockStatus(false);
        return item;
    }

    private boolean isDeletedRow(QuoteDetail detail) {
        return detail.getQuantity() != null && detail.getQuantity().compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 判断报价单有没有被修改过
     * @param newDetail
     * @param oldDetail
     * @return
     */
    private boolean isMarkedAsNewByVersion(QuoteDetail newDetail, QuoteDetail oldDetail) {
        if (oldDetail == null) {
            return true;
        }

        return !Objects.equals(newDetail.getLockVersion(), oldDetail.getLockVersion());
    }

    /**
     * 返回新旧报价单的唯一对应key
     * @param detail
     * @return
     */
    private String buildDetailIdentityKey(QuoteDetail detail) {
        if (detail.getRowNum() != null) {
            return "ROW|" + detail.getRowNum();
        }
        String product = Optional.ofNullable(detail.getProductName()).orElse("").trim();
        String spec = Optional.ofNullable(detail.getSpec()).orElse("").trim();
        String model = Optional.ofNullable(detail.getModel()).orElse("").trim();
        String unit = Optional.ofNullable(detail.getUnit()).orElse("").trim();
        return "FALLBACK|" + product + "|" + spec + "|" + model + "|" + unit;
    }

    /**
     * 旧业务单与旧报价单组合体
     */
    private static class OldBusinessContext {
        private final QuoteBusinessItem item;
        private final QuoteDetail detail;

        private OldBusinessContext(QuoteBusinessItem item, QuoteDetail detail) {
            this.item = item;
            this.detail = detail;
        }
    }
}
