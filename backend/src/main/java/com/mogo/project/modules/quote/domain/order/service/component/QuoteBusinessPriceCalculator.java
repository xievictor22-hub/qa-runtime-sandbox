package com.mogo.project.modules.quote.domain.order.service.component;


import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.modules.quote.domain.order.entity.QuoteBusinessItem;
import com.mogo.project.modules.quote.domain.order.entity.QuoteDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 业务阶段价格计算器：统一初始化与重算口径，避免脏数据
 */
@Component
public class QuoteBusinessPriceCalculator {

    public static final BigDecimal ONE = BigDecimal.ONE;
    public static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * 按报价明细初始化业务明细，并进行一次重算
     */
    public QuoteBusinessItem initFromDetail(Long quoteId, Integer businessVersion, QuoteDetail detail) {
        QuoteBusinessItem item = new QuoteBusinessItem();
        item.setQuoteId(quoteId);
        item.setDetailId(detail.getId());
        item.setBusinessVersion(businessVersion);

        item.setSalesUnitPrice(nvl(detail.getDistPrice()));
        item.setSalesDiscount(ONE);
        item.setSalesPoint(ZERO);
        item.setSalesUnitAdjustAmount(ZERO);

        item.setFactorySalesAdjustAmount(ZERO);
        item.setFactoryCostUnitPrice(nvl(detail.getFactoryCostUnitPrice()));
        item.setFactoryDiscounts(nvl(detail.getFactoryDiscounts()));
        item.setFactoryUnitPrice(nvl(detail.getFactoryUnitPrice()));
        item.setFactoryTotal(nvl(detail.getFactoryTotal()));
        item.setFactoryProfit(nvl(detail.getFactoryProfit()));
        item.setFactoryProfitRate(nvl(detail.getFactoryProfitRate()));
        item.setCustomerFactoryTotal(nvl(detail.getFactoryTotal()));
        item.setAdjustedFactoryProfitAmount(nvl(detail.getFactoryProfit()));
        item.setAdjustedFactoryProfitRate(ZERO);

        item.setInstallSalesAdjustAmount(ZERO);
        item.setInstallCostUnitPrice(nvl(detail.getInstallCostUnitPrice()));
        item.setInstallDiscounts(nvl(detail.getInstallDiscounts()));
        item.setInstallUnitPrice(nvl(detail.getInstallUnitPrice()));
        item.setInstallTotal(nvl(detail.getInstallTotal()));
        item.setInstallProfit(nvl(detail.getInstallProfit()));
        item.setInstallProfitRate(nvl(detail.getInstallProfitRate()));
        item.setCustomerInstallTotal(nvl(detail.getInstallTotal()));
        item.setAdjustedInstallProfitAmount(nvl(detail.getInstallProfit()));
        item.setAdjustedInstallProfitRate(ZERO);

        item.setLockStatus(false);

        recalculate(item, null);
        return item;
    }

    /**
     * 重算派生字段（可选传入数量）
     */
    public void recalculate(QuoteBusinessItem item, BigDecimal quantity) {
        validate(item);

        BigDecimal salesDiscountedUnitPrice = nvl(item.getSalesUnitPrice())
                .multiply(nvl(item.getSalesDiscount()))
                .setScale(2, RoundingMode.HALF_UP);
        item.setSalesDiscountedUnitPrice(salesDiscountedUnitPrice);

        BigDecimal customerUnitPrice = salesDiscountedUnitPrice
                .add(nvl(item.getSalesUnitAdjustAmount()))
                .setScale(2, RoundingMode.HALF_UP);
        item.setCustomerUnitPrice(customerUnitPrice);

        if (quantity != null) {
            item.setSalesTotal(salesDiscountedUnitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP));
            item.setCustomerTotalPrice(customerUnitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP));
        } else {
            if (item.getSalesTotal() == null) item.setSalesTotal(ZERO);
            if (item.getCustomerTotalPrice() == null) item.setCustomerTotalPrice(ZERO);
        }

        item.setCustomerFactoryTotal(nvl(item.getCustomerFactoryTotal()).add(nvl(item.getFactorySalesAdjustAmount())).setScale(2, RoundingMode.HALF_UP));
        item.setCustomerInstallTotal(nvl(item.getCustomerInstallTotal()).add(nvl(item.getInstallSalesAdjustAmount())).setScale(2, RoundingMode.HALF_UP));

        if (nvl(item.getCustomerFactoryTotal()).compareTo(ZERO) != 0) {
            item.setAdjustedFactoryProfitRate(
                    nvl(item.getAdjustedFactoryProfitAmount())
                            .divide(nvl(item.getCustomerFactoryTotal()), 4, RoundingMode.HALF_UP)
            );
        } else {
            item.setAdjustedFactoryProfitRate(ZERO);
        }

        if (nvl(item.getCustomerInstallTotal()).compareTo(ZERO) != 0) {
            item.setAdjustedInstallProfitRate(
                    nvl(item.getAdjustedInstallProfitAmount())
                            .divide(nvl(item.getCustomerInstallTotal()), 4, RoundingMode.HALF_UP)
            );
        } else {
            item.setAdjustedInstallProfitRate(ZERO);
        }
    }

    public void validate(QuoteBusinessItem item) {
        if (item == null) throw new ServiceException("业务明细不能为空");
        if (item.getId() == null && (item.getQuoteId() == null || item.getDetailId() == null)) {
            throw new ServiceException("业务明细缺少主键信息");
        }
        if (nvl(item.getSalesDiscount()).compareTo(ZERO) < 0) {
            throw new ServiceException("销售折扣不能小于0");
        }
        if (nvl(item.getSalesDiscount()).compareTo(new BigDecimal("10")) > 0) {
            throw new ServiceException("销售折扣异常，请使用倍率口径(如1=100%)");
        }
    }

    private BigDecimal nvl(BigDecimal v) {
        return v == null ? ZERO : v;
    }
}
