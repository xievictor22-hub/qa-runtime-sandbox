package com.mogo.project.modules.quote.domain.order.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableField;
import com.mogo.project.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 业务员调整字段
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuoteBusinessItem extends BaseEntity   {

    /**
     * 报价单ID
     */
    private Long quoteId;

    /**
     * 关联的报价员明细ID
     */
    private Long detailId;

    /**
     * 业务调整版本号
     */
    private Integer businessVersion;

    /** 生产销售调整金额 */
    private BigDecimal factorySalesAdjustAmount;
    /** 生产成本单价 */
    private BigDecimal factoryCostUnitPrice;
    /** 生产折扣 */
    private BigDecimal factoryDiscounts;
    /** 生产单价 */
    private BigDecimal factoryUnitPrice;
    /** 生产总价 */
    private BigDecimal factoryTotal;
    /** 生产利润 */
    private BigDecimal factoryProfit;
    /** 生产打点 */
    private BigDecimal factoryProfitRate;

    /** 客户生产总价 */
    private BigDecimal customerFactoryTotal;
    /** 调整生产后利润金额 */
    private BigDecimal adjustedFactoryProfitAmount;
    /** 调整后生产利润率 */
    private BigDecimal adjustedFactoryProfitRate;

    /** 安装销售调整金额 */
    private BigDecimal installSalesAdjustAmount;
    /** 安装成本单价 */
    private BigDecimal installCostUnitPrice;
    /** 安装折扣 */
    private BigDecimal installDiscounts;
    /** 安装单价 */
    private BigDecimal installUnitPrice;
    /** 安装总价 */
    private BigDecimal installTotal;
    /** 安装利润 */
    private BigDecimal installProfit;
    /** 安装打点 */
    private BigDecimal installProfitRate;

    /** 客户安装总价 */
    private BigDecimal customerInstallTotal;
    /** 调整安装后利润金额 */
    private BigDecimal adjustedInstallProfitAmount;
    /** 调整后安装利润率 */
    private BigDecimal adjustedInstallProfitRate;

    /** 销售单价(初始化来自 QuoteDetail.distPrice) */
    private BigDecimal salesUnitPrice;
    /** 销售打点 */
    private BigDecimal salesPoint;
    /** 销售折扣 */
    private BigDecimal salesDiscount;
    /** 折后销售单价 */
    private BigDecimal salesDiscountedUnitPrice;
    /** 销售总价 */
    private BigDecimal salesTotal;
    /** 销售利润 */
    private BigDecimal salesProfit;
    /** 对客户单价 */
    private BigDecimal customerUnitPrice;
    /** 销售单价调整 */
    private BigDecimal salesUnitAdjustAmount;
    /** 对客户总价 */
    private BigDecimal customerTotalPrice;

    /**
     * 锁定状态(0:未锁, 1:已锁)
     */
    private Boolean lockStatus;

    /**
     *
     */
    private String remark;

    //产品名称
    @TableField(exist = false)
    private String productName;
    //规格
    @TableField(exist = false)
    private String spec;
    //单位
    @TableField(exist = false)
    private String unit;
    //数量
    @TableField(exist = false)
    private BigDecimal quantity;


}
