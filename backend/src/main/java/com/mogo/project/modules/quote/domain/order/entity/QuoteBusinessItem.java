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

    /**
     * 原总价(冗余字段，方便快照)
     */
    private BigDecimal originalTotal;

    /**
     * 折扣率(%)
     */
    private BigDecimal discountRate;

    /**
     * 折扣后总价
     */
    private BigDecimal discountTotal;

    /**
     * 最终调整总价(人工一口价)
     */
    private BigDecimal finalTotal;

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