package com.mogo.project.common.excel.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;

/**
 * easyExcel 实例 性别转换器：数据库 Integer -> Excel String
 */
public class GenderConverter implements Converter<Integer> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /**
     * 导入转换：Excel(男) -> Java(1)
     */
    @Override
    public Integer convertToJavaData(ReadConverterContext<?> context) {
        String cellValue = context.getReadCellData().getStringValue();
        return "男".equals(cellValue) ? 1 : "女".equals(cellValue) ?2:0 ; // 简单示例，建议结合枚举
    }

    /**
     * 导出转换：Java(1) -> Excel(男)
     */
    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Integer> context) {
        Integer value = context.getValue();
        if (value == null) {
            return new WriteCellData<>("");
        }
        return new WriteCellData<>(value == 1 ? "男" : value == 2 ? "女":"");
    }
}