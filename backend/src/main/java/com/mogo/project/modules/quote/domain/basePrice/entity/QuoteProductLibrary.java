package com.mogo.project.modules.quote.domain.basePrice.entity;



import com.baomidou.mybatisplus.annotation.TableName;
import com.mogo.project.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 制品底价表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quote_product_library")
public class QuoteProductLibrary extends BaseEntity {

    private Long id;
    private String version;
    private String batchId;

    private Integer projectType;
    private String process1;
    private String process2;
    private String process3;
    private String process4;
    private String unit;

    /** 范围展示文本 */
    private String judgeVal;
    private String wVal;
    private String dVal;

    /** 损耗系数 (DECIMAL) */
    private BigDecimal quoteFormula;

    /** 基础单价 (DECIMAL) */
    private BigDecimal unitPrice;

    /** 打点系数 */
    private BigDecimal pointCoefficient;
}
