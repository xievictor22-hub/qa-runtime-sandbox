package com.mogo.project.modules.quote.domain.basePrice.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.mogo.project.modules.quote.core.excelConverter.ProjectTypeConverter;
import lombok.Data;

import java.math.BigDecimal;

@Data
@HeadRowHeight(20) // 表头高度
@ColumnWidth(20)   // 默认列宽
public class QuoteBasePriceExportExcelVo {

    @ExcelProperty(value = "项目类型",converter = ProjectTypeConverter.class) // 0:家装, 1:工装 (建议Excel下拉框填文本，转换器转int，这里简化为填数字)
    private Integer projectType;

    @ExcelProperty("项目1")
    private String process1;

    @ExcelProperty("项目2")
    private String process2;

    @ExcelProperty("项目3")
    private String process3;

    @ExcelProperty("项目4")
    private String process4;

    @ExcelProperty("单位")
    private String unit;

    @ColumnWidth(30)
    @ExcelProperty("判断范围值")
    private String rangeValC;

    @ExcelProperty("W取值(宽)")
    private String rangeValW;

    @ExcelProperty("D取值(厚)")
    private String rangeValD;

    @ExcelProperty("报价公式") // 对应 quote_formula
    private BigDecimal quoteFormula;

    @ExcelProperty("打点系数")
    private BigDecimal pointCoefficient;

    @ExcelProperty("单价")
    private BigDecimal unitPrice;

    @ExcelProperty("是否计入折件") // 0, 2, 3, 4
    private Integer isFolding;

    // 如果需要版本号
    @ExcelProperty("报价版本")
    private String version;

    @ExcelProperty("备注")
    private String remarkMessage;


}
