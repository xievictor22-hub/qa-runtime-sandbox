package com.mogo.project.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.system.model.dto.RoleQueryDto;
import com.mogo.project.modules.system.model.entity.SysRole;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {
    IPage<SysRole> getRolePage(RoleQueryDto queryDto);
    boolean saveRole(SysRole role);
    boolean updateRole(SysRole role);
    boolean deleteRole(Long id);
    List<SysRole> getAllRoles(); // 给用户分配角色时用
    /** 获取角色拥有的菜单ID集合 */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /** 给角色分配菜单 */
    void assignMenus(Long roleId, List<Long> menuIds);

    /** 回显role_dept信息 */
    List<Long> getDeptIdsByRoleId(Long roleId);
}