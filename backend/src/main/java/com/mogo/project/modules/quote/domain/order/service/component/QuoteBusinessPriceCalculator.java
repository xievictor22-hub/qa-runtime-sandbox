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

        BigDecimal quantity = nvl(detail.getQuantity());

        // 生产段初始化
        item.setFactoryCostUnitPrice(nvl(detail.getFactoryCostUnitPrice()));
        item.setFactoryProfitRate(ZERO);
        item.setFactoryDiscounts(ONE);
        item.setFactoryUnitPrice(mul2(item.getFactoryCostUnitPrice(), ONE.add(item.getFactoryProfitRate())));
        item.setFactoryTotal(mul2(item.getFactoryUnitPrice(), quantity));
        item.setFactoryProfit(mul2(item.getFactoryCostUnitPrice().multiply(item.getFactoryProfitRate()), quantity));
        item.setFactorySalesAdjustAmount(ZERO);
        item.setCustomerFactoryTotal(item.getFactoryTotal());

        // 安装段初始化
        item.setInstallCostUnitPrice(nvl(detail.getInstallCostUnitPrice()));
        item.setInstallProfitRate(ZERO);
        item.setInstallDiscounts(ONE);
        item.setInstallUnitPrice(mul2(item.getInstallCostUnitPrice(), ONE.add(item.getInstallProfitRate())));
        item.setInstallTotal(mul2(item.getInstallUnitPrice(), quantity));
        item.setInstallProfit(mul2(item.getInstallCostUnitPrice().multiply(item.getInstallProfitRate()), quantity));
        item.setInstallSalesAdjustAmount(ZERO);
        item.setCustomerInstallTotal(item.getInstallTotal());

        // 销售段初始化
        item.setSalesCostUnitPrice(item.getFactoryUnitPrice().add(item.getInstallUnitPrice()).setScale(2, RoundingMode.HALF_UP));
        item.setSalesPoint(ZERO);
        item.setSalesDiscount(ONE);
        item.setSalesUnitPrice(mul2(item.getSalesCostUnitPrice(), ONE.add(item.getSalesPoint())));
        item.setSalesDiscountedUnitPrice(mul2(item.getSalesUnitPrice(), item.getSalesDiscount()));
        item.setSalesTotal(mul2(item.getSalesDiscountedUnitPrice(), quantity));
        item.setSalesProfit(ZERO);
        item.setCustomerUnitPrice(item.getSalesUnitPrice());
        item.setSalesUnitAdjustAmount(ZERO);
        item.setCustomerTotalPrice(mul2(item.getCustomerUnitPrice(), quantity));

        // 调整利润段初始化
        item.setAdjustedFactoryProfitAmount(item.getFactoryProfit());
        item.setAdjustedInstallProfitAmount(item.getInstallProfit());

        item.setLockStatus(false);

        recalculate(item, quantity);
        return item;
    }

    /**
     * 重算派生字段（可选传入数量）
     */
    public void recalculate(QuoteBusinessItem item, BigDecimal quantity) {
        validate(item);

        BigDecimal qty = quantity == null ? ZERO : quantity;

        item.setFactoryUnitPrice(mul2(nvl(item.getFactoryCostUnitPrice()), ONE.add(nvl(item.getFactoryProfitRate()))));
        item.setFactoryTotal(mul2(nvl(item.getFactoryUnitPrice()), qty));
        item.setFactoryProfit(mul2(nvl(item.getFactoryCostUnitPrice()).multiply(nvl(item.getFactoryProfitRate())), qty));
        item.setCustomerFactoryTotal(nvl(item.getCustomerFactoryTotal()).setScale(2, RoundingMode.HALF_UP));
        item.setAdjustedFactoryProfitAmount(nvl(item.getCustomerFactoryTotal())
                .subtract(nvl(item.getFactoryCostUnitPrice()).multiply(qty)).setScale(2, RoundingMode.HALF_UP));

        item.setInstallUnitPrice(mul2(nvl(item.getInstallCostUnitPrice()), ONE.add(nvl(item.getInstallProfitRate()))));
        item.setInstallTotal(mul2(nvl(item.getInstallUnitPrice()), qty));
        item.setInstallProfit(mul2(nvl(item.getInstallCostUnitPrice()).multiply(nvl(item.getInstallProfitRate())), qty));
        item.setCustomerInstallTotal(nvl(item.getCustomerInstallTotal()).setScale(2, RoundingMode.HALF_UP));
        item.setAdjustedInstallProfitAmount(nvl(item.getCustomerInstallTotal())
                .subtract(nvl(item.getInstallCostUnitPrice()).multiply(qty)).setScale(2, RoundingMode.HALF_UP));

        item.setSalesCostUnitPrice(nvl(item.getFactoryUnitPrice()).add(nvl(item.getInstallUnitPrice())).setScale(2, RoundingMode.HALF_UP));
        item.setSalesUnitPrice(mul2(nvl(item.getSalesCostUnitPrice()), ONE.add(nvl(item.getSalesPoint()))));
        BigDecimal salesDiscountedUnitPrice = mul2(nvl(item.getSalesUnitPrice()), ONE);
        item.setSalesDiscountedUnitPrice(salesDiscountedUnitPrice);

        BigDecimal customerUnitPrice = nvl(item.getCustomerUnitPrice()).setScale(2, RoundingMode.HALF_UP);
        item.setCustomerUnitPrice(customerUnitPrice);
        item.setSalesUnitAdjustAmount(customerUnitPrice.subtract(nvl(item.getSalesUnitPrice())).setScale(2, RoundingMode.HALF_UP));

        item.setSalesTotal(mul2(salesDiscountedUnitPrice, qty));
        item.setCustomerTotalPrice(mul2(customerUnitPrice, qty));

        BigDecimal factoryBase = nvl(item.getFactoryCostUnitPrice()).multiply(qty);
        item.setAdjustedFactoryProfitRate(factoryBase.compareTo(ZERO) == 0 ? ZERO :
                nvl(item.getAdjustedFactoryProfitAmount()).divide(factoryBase, 4, RoundingMode.HALF_UP));

        BigDecimal installBase = nvl(item.getInstallCostUnitPrice()).multiply(qty);
        item.setAdjustedInstallProfitRate(installBase.compareTo(ZERO) == 0 ? ZERO :
                nvl(item.getAdjustedInstallProfitAmount()).divide(installBase, 4, RoundingMode.HALF_UP));
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


    private BigDecimal mul2(BigDecimal a, BigDecimal b) {
        return nvl(a).multiply(nvl(b)).setScale(2, RoundingMode.HALF_UP);
    }
}
