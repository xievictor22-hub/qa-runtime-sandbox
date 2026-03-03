package com.mogo.project.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mogo.project.common.entity.BaseEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// ... imports
@TableName("sys_dept")
@Data
public class SysDept extends BaseEntity {
    private Long parentId;
    private String ancestors; // 祖级列表，如 0,100,101
    private String deptName;
    private Integer orderNum;
    private String phone;
    private String email;
    private Integer status;
    private Long leaderId;
    @TableField(exist = false)
    private String leaderName;

    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();
}