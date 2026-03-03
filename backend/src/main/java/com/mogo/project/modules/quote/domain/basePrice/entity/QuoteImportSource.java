package com.mogo.project.modules.quote.domain.basePrice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mogo.project.common.entity.BaseEntity; // 假设你已有 BaseEntity
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 报价原始导入表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quote_import_source")
public class QuoteImportSource extends BaseEntity {

    private Long id;
    private String batchId;
    private String version;

    private Integer projectType;
    private String process1;
    private String process2;
    private String process3;
    private String process4;
    private String unit;

    // 原始范围文本
    private String rangeValC;
    private String rangeValW;
    private String rangeValD;

    // 核心数值: 损耗系数 (Decimal)
    private BigDecimal quoteFormula;
    private BigDecimal pointCoefficient;
    private BigDecimal unitPrice;

    // 是否计入折件
    private Integer isFolding;
    //导入表行号
    private Integer sort;
    //备注
    private String remarkMessage;
}
