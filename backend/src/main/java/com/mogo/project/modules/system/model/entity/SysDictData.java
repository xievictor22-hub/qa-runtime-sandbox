package com.mogo.project.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict_data")
public class SysDictData implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long dictCode;

    private Integer dictSort;
    private String dictLabel;
    private String dictValue;
    private String dictType;

    // 增强字段
    private String listClass; // UI样式：success, info, warning, danger
    private String cssClass;
    private String isDefault;
    private String authCode;  // 权限标识

    private String status; // 0正常 1停用
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}