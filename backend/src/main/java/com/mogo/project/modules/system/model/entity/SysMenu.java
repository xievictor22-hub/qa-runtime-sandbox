package com.mogo.project.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mogo.project.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 父菜单ID (0为顶级菜单) */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 显示顺序 */
    private Integer sort;

    /** 路由地址 (前端路径) */
    private String path;

    /** 组件路径 (Vue组件文件路径) */
    private String component;

    /** 权限标识 (如: system:user:list) */
    private String perms;

    /** 菜单图标 */
    private String icon;

    /** 菜单类型 (M:目录 C:菜单 F:按钮) */
    private String menuType;

    /** 状态 (1正常 0停用) */
    private Integer status;

    /** 是否隐藏 (1是 0否) */
    private Integer visible;

    // --- 辅助字段 (数据库不存在) ---
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}