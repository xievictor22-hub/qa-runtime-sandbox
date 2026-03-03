package com.mogo.project.modules.quote.domain.basePrice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mogo.project.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 折件底价表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quote_folding_rule")
public class QuoteFoldingRule extends BaseEntity {

    private Long id;
    private String version;
    private String batchId;

    private Integer projectType;
    private String process1;
    private String process2;

    // --- W 宽范围 ---
    private BigDecimal wMin;
    private BigDecimal wMax;
    private Integer wMinInclude;
    private Integer wMaxInclude;

    // --- D 厚范围 ---
    private BigDecimal dMin;
    private BigDecimal dMax;
    private Integer dMinInclude;
    private Integer dMaxInclude;

    // --- C 刀数范围 ---
    private BigDecimal cMin;
    private BigDecimal cMax;
    private Integer cMinInclude;
    private Integer cMaxInclude;

    private String unit;

    /** 计算公式字符串(如 H*W/1000*1.08) */
    private String quoteFormula;

    private BigDecimal unitPrice;
    private BigDecimal pointCoefficient;
}