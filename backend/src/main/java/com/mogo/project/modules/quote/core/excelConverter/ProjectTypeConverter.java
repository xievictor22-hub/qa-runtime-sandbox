package com.mogo.project.modules.quote.core.excelConverter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;

/**
 * 表格中家装工装转为0，1
 */
public class ProjectTypeConverter implements Converter<Integer> {
    @Override
    public Class<?> supportJavaTypeKey() {
        // Java 中用 Integer 接收
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        // Excel 中是字符串格式
        return CellDataTypeEnum.STRING;
    }

    /**
     * 【读】Excel -> Java
     * 读取时，把 "工装" 转为 1，"家装" 转为 0
     */
    @Override
    public Integer convertToJavaData(ReadConverterContext<?> context) {
        String cellValue = context.getReadCellData().getStringValue();
        if (cellValue == null) {
            return null;
        }
        // 建议加上 trim() 防止 Excel 里有空格
        String val = cellValue.trim();

        if ("工装".equals(val)) {
            return 1;
        } else if ("家装".equals(val)) {
            return 0;
        }
        // 如果填了其他乱七八糟的，可以返回 null 或者抛异常，或者默认为家装
        return 0;
    }

    /**
     * 【写】Java -> Excel
     * 导出时，把 1 转为 "工装"，0 转为 "家装"
     */
    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Integer> context) {
        Integer value = context.getValue();
        if (value == null) {
            return new WriteCellData<>("");
        }
        if (value == 1) {
            return new WriteCellData<>("工装");
        }
        return new WriteCellData<>("家装");
    }
}
