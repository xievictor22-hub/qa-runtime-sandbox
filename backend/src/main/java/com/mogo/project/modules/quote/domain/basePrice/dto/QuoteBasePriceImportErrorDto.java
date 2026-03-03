package com.mogo.project.modules.quote.domain.basePrice.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;

import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导入表出错时的回调表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QuoteBasePriceImportErrorDto extends QuoteBasePriceImportExcelDto {

    @ExcelProperty(value = "错误提示",index = 19) // index设大一点，保证在最后一列
    @ColumnWidth(50) // 宽度设置大一点以便阅读
    // 设置背景色为红色，字体白色，起到警示作用
    @ContentStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND, fillForegroundColor = 13)
    private String errorMsg;
}