package com.mogo.project.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    private String email;

    private String phone;

    /**
     * 状态 1:正常 0:禁用
     */
    private Integer status;
    /**
     * 性别  0:保密 1:男 2：女
     */
    private Integer gender;

    /**
     * 工号
     */
    private String workId;

    /**
     * 逻辑删除 0:未删 1:已删
     */
    @TableLogic
    private Integer deleteFlag;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Long deptId;


    @TableField(exist = false)
    private SysDept dept;

    /**
     * 部门全路径名称 (前端展示用)
     * 格式：总公司 / 生产中心 / 制作组
     */
    @TableField(exist = false)
    private String deptNamePath;

    @TableField(exist = false)
    private List<SysRole> Roles = new ArrayList<>();


    /** * 临时字段：该用户拥有的所有角色名称 (逗号分隔)
     */
    @TableField(exist = false)
    private String roleNames;

}