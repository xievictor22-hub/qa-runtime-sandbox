package com.mogo.project.modules.quote.domain.basePrice.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 报价单导入 DTO
 * 对应 Excel 文件的列头
 */
@Data
public class QuoteImportDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("行号")
    private Integer rowNum;

    @ExcelProperty("一级分类")
    private String categoryLvl1;

    @ExcelProperty("二级分类")
    private String categoryLvl2;

    @ExcelProperty("区域")
    private String projectArea;

    @ExcelProperty("位置")
    private String position;

    @ExcelProperty("产品名称")
    private String productName;

    @ExcelProperty("材料")
    private String material;

    @ExcelProperty("规格描述")
    private String spec;

    @ExcelProperty("宽(mm)")
    private BigDecimal width;

    @ExcelProperty("长(mm)")
    private BigDecimal length;

    @ExcelProperty("厚(mm)")
    private String thickness; // 厚度有时包含单位，用String较安全，或者用BigDecimal

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("数量")
    private Integer quantity;

    @ExcelProperty("出厂单价")
    private BigDecimal factoryPrice;

    @ExcelProperty("出厂总价")
    private BigDecimal factoryTotal;

    @ExcelProperty("安装单价")
    private BigDecimal installPrice;

    @ExcelProperty("安装总价")
    private BigDecimal installTotal;

    @ExcelProperty("备注")
    private String remarkDesc;
}