package com.mogo.project.modules.system.model.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.mogo.project.common.excel.converter.GenderConverter;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ColumnWidth(20) // 全局列宽
public class UserExcelVO implements Serializable {

    @ExcelProperty("用户编号") // 表头名称
    private Long id;

    @ExcelProperty("用户姓名")
    private String nickname;

    @ExcelProperty("用户姓名")
    private String username;

    /**
     * 使用自定义转换器：1->男, 0->女
     */
    @ExcelProperty(value = "用户性别", converter = GenderConverter.class)
    private Integer gender;

    @ExcelProperty("手机号码")
    @ColumnWidth(15) // 单独指定列宽
    private String phone;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelIgnore // 忽略此字段，不导出
    private String password;
}