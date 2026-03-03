package com.mogo.project.modules.quote.domain.basePrice.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 报价单导出 DTO
 * 对应 EasyExcel 的导出模型
 */
@Data
@ColumnWidth(20) // 默认列宽
public class QuoteExportDto {

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

    @ExcelProperty("宽 (mm)")
    private BigDecimal width;

    @ExcelProperty("长 (mm)")
    private BigDecimal length;

    @ExcelProperty("厚 (mm)")
    private String thickness;

    @ExcelProperty("单位")
    private String unit;

    @ExcelProperty("数量")
    private Integer quantity;

    @ExcelProperty("单价 (元)")
    private BigDecimal price; // 对应 factoryPrice 或 salesPrice，视导出场景而定

    @ExcelProperty("总价 (元)")
    private BigDecimal total;

    @ExcelProperty("备注")
    private String remarkDesc;
}