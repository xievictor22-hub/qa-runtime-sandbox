package com.mogo.project.modules.quote.domain.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mogo.project.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 报价制品子件表
 * (对应数据库表: quote_detail_item)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quote_detail_item")
@Schema(description = "报价制品子件/配方明细")
public class QuoteDetailItem extends BaseEntity {

    private Long quoteId;
    private Long detailId;

    /** 来源底价库ID (仅供追溯) */
    private Long sourceLibraryId;
    /** 来源版本号 */
    private String version;

    // --- 快照字段 (Snapshot) ---
    private String process1;//项目1
    private String process2;
    private String process3;
    private String process4;
    private String unit;

    /** 快照:单价 存储报价底表信息，预防报价底表变更后没存根 */
    private BigDecimal unitPriceSnapshot;
    /** 快照:公式或系数 存储报价底表信息，预防报价底表变更后没存根 */
    private String formulaSnapshot;
    /** 快照:打点系数 存储报价底表信息，预防报价底表变更后没存根*/
    private BigDecimal pointCoefficientSnapshot;

    // --- 计算结果 ---
    /** 单价 */
    private BigDecimal distPrice;
    /** 数量 */
    private BigDecimal quantity;
    /** 汇总价 */
    private BigDecimal totalPrice;


    /** 内部技术备注 (BaseEntity里的remark可能用于其他用途，这里区分一下) */
    @TableField("remark_desc")
    private String remarkDesc;
}