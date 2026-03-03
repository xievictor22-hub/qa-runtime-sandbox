package com.mogo.project.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mogo.project.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 角色名称 (如：超级管理员) */
    private String roleName;

    /** 角色权限字符串 (如：admin) */
    private String roleKey;

    /** 显示顺序 */
    private Integer sort;

    /** 角色状态 (1正常 0停用) */
    private Integer status;

    /**角色表增加 数据范围 字段
     -- 1:全部数据权限 2:自定数据权限 3:本部门数据权限 4:本部门及以下数据权限 5:仅本人数据权限 */
    private String  dataScope;

    /** 菜单组（用于前端传参） */
    @TableField(exist = false)
    private List<Long> menuIds;

    /** 部门组（数据权限-自定义时使用） */
    @TableField(exist = false)
    private List<Long> deptIds;
}