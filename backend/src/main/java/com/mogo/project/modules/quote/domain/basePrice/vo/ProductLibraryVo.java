package com.mogo.project.modules.quote.domain.basePrice.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductLibraryVo {
    private Long id;
    private String process1;
    private String process2;
    private String process3;
    private String process4;
    private String unit;

    /** 损耗系数 (DECIMAL) */
    private BigDecimal quoteFormula;

    /** 基础单价 (DECIMAL) */
    private BigDecimal unitPrice;

    /** 打点系数 */
    private BigDecimal pointCoefficient;
}
