package com.mogo.project.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.system.model.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {
    /** 获取菜单树形列表 (管理端使用) */
    List<SysMenu> getMenuTree(String menuName, Integer status);

    /** 校验菜单名称唯一 */
    boolean checkMenuNameUnique(SysMenu menu);

    /** 是否存在子菜单 */
    boolean hasChildByMenuId(Long menuId);

    List<SysMenu> selectMenuTreeByUserId(Long userId);
}