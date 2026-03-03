package com.mogo.project.modules.quote.domain.basePrice.dto;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.mogo.project.modules.quote.core.excelConverter.ProjectTypeConverter;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.math.BigDecimal;

/**
 * 底价导入表 / 单个添加参数对象
 * 兼顾 EasyExcel 导入与 API 单个新增校验
 */
@Data
public class QuoteBasePriceImportExcelDto {

    /**
     * 项目类型: 0-家装, 1-工装
     */
    @ExcelProperty(value = "项目类型", converter = ProjectTypeConverter.class)
    @NotNull(message = "项目类型不能为空")
    @Range(min = 0, max = 1, message = "项目类型只能是 0(家装) 或 1(工装)")
    private Integer projectType;

    @ExcelProperty("项目1")
    @NotBlank(message = "项目1不能为空")
    private String process1;

    @ExcelProperty("项目2")
    private String process2;

    @ExcelProperty("项目3")
    private String process3;

    @ExcelProperty("项目4")
    private String process4;

    @ExcelProperty("单位")
    @NotBlank(message = "单位不能为空")
    private String unit;

    @ExcelProperty("判断范围值")
    private String rangeValC;

    @ExcelProperty("W取值(宽)")
    private String rangeValW;

    @ExcelProperty("D取值(厚)")
    private String rangeValD;

    /**
     * 报价公式 (损耗系数)
     */
    @ExcelProperty("报价公式")
    @NotNull(message = "报价公式(损耗系数)不能为空")
    @DecimalMin(value = "0.0", inclusive = true, message = "损耗系数必须大于等于0")
    private BigDecimal quoteFormula;


    @DecimalMin(value = "0.0", inclusive = true, message = "打点系数必须大于等于0")
    @ExcelProperty("打点系数")
    private BigDecimal pointCoefficient;

    /**
     * 单价
     */
    @ExcelProperty("单价")
    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.0", inclusive = true, message = "单价必须大于等于0")
    private BigDecimal unitPrice;

    /**
     * 是否计入折件: 0-否, 1-4 (具体类型)
     */
    @ExcelProperty("是否计入折件")
    @NotNull(message = "是否计入折件不能为空")
    @Range(min = 0, max = 4, message = "是否计入折件取值必须在 0 到 4 之间")
    private Integer isFolding;

    @ExcelProperty("备注")
    private String remarkMessage;

    @ExcelIgnore
    private Integer sort;
}