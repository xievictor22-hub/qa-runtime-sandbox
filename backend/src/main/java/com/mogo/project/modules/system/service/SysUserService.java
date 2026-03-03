package com.mogo.project.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.system.model.entity.SysUser; // 注意引用之前移动过的实体路径
import com.mogo.project.modules.system.model.dto.UserQueryDto;

import java.util.List;

public interface SysUserService extends IService<SysUser> {

    // 分页查询用户
    IPage<SysUser> getUserPage(UserQueryDto queryDto);

    // 新增用户
    boolean saveUser(SysUser sysUser);

    // 修改用户
    boolean updateUser(SysUser sysUser);

    /** 根据用户ID获取已分配的角色ID列表 */
    List<Long> getRoleIdsByUserId(Long userId);

    /** 用户分配角色 */
    void assignRoles(Long userId, List<Long> roleIds);

    List<SysUser> getAllUserList();

    List<SysUser> selectUserListByRoleKey(String roleKey);
}