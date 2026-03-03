package com.mogo.project.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.modules.system.mapper.SysRoleDeptMapper;
import com.mogo.project.modules.system.model.dto.RoleQueryDto;
import com.mogo.project.modules.system.model.entity.SysRole;
import com.mogo.project.modules.system.model.entity.SysRoleDept;
import com.mogo.project.modules.system.model.entity.SysRoleMenu;
import com.mogo.project.modules.system.mapper.SysRoleMapper;
import com.mogo.project.modules.system.mapper.SysRoleMenuMapper;
import com.mogo.project.modules.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.glassfish.jaxb.core.v2.TODO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMenuMapper sysRoleMenuMapper;

    private final SysRoleDeptMapper sysRoleDeptMapper;

    @Override
    public IPage<SysRole> getRolePage(RoleQueryDto queryDto) {
        Page<SysRole> page = new Page<>(queryDto.getPageNum(), queryDto.getPageSize());
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(StringUtils.hasText(queryDto.getRoleName()), SysRole::getRoleName, queryDto.getRoleName())
                .like(StringUtils.hasText(queryDto.getRoleKey()), SysRole::getRoleKey, queryDto.getRoleKey())
                .eq(queryDto.getStatus() != null, SysRole::getStatus, queryDto.getStatus())
                .orderByAsc(SysRole::getSort); // 角色通常按 sort 排序

        return this.page(page, wrapper);
    }

    @Override
    public boolean saveRole(SysRole role) {
        if (checkNameUnique(role.getRoleName(), null)) {
            throw new RuntimeException("角色名称已存在");
        }
        if (checkKeyUnique(role.getRoleKey(), null)) {
            throw new RuntimeException("权限字符已存在");
        }
        // 初始化
        if (role.getStatus() == null) role.setStatus(1);
        if (role.getSort() == null) role.setSort(0);

        boolean result = this.save(role);
        //维护自定义权限的role_dept
        insertRoleDept(role);
        return result;
    }

    @Override
    public boolean updateRole(SysRole role) {
        if (checkNameUnique(role.getRoleName(), role.getId())) {
            throw new RuntimeException("角色名称已存在");
        }
        if (checkKeyUnique(role.getRoleKey(), role.getId())) {
            throw new RuntimeException("权限字符已存在");
        }
        boolean result = this.updateById(role);

        //维护自定义权限的role_dept
        // 新增：先删后插
        sysRoleDeptMapper.delete(new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, role.getId()));
        insertRoleDept(role);

        return result;
    }

    @Override
    public boolean deleteRole(Long id) {
        // TODO: 后续需要检查该角色下是否有用户，如果有则不能删除
        return this.removeById(id);
    }

    @Override
    public List<SysRole> getAllRoles() {
        // 仅查询正常的角色
        return this.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSort));
    }

    /** 校验角色名唯一 (排除自身ID) */
    private boolean checkNameUnique(String roleName, Long id) {
        return this.count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleName, roleName)
                .ne(id != null, SysRole::getId, id)) > 0;
    }

    /** 校验权限字符唯一 (排除自身ID) */
    private boolean checkKeyUnique(String roleKey, Long id) {
        return this.count(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, roleKey)
                .ne(id != null, SysRole::getId, id)) > 0;
    }
    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
        ).stream().map(SysRoleMenu::getMenuId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 1. 先删除原有关系
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));

        // 2. 批量插入新关系
        if (menuIds != null && !menuIds.isEmpty()) {
            menuIds.forEach(menuId -> {
                sysRoleMenuMapper.insert(new SysRoleMenu(roleId, menuId));
            });
        }
    }

    // 辅助方法 维护自定义权限， role_dept
    private void insertRoleDept(SysRole role) {
        // 只有当数据权限为 "2" (自定义) 且 deptIds 不为空时才插入
        if ("2".equals(role.getDataScope()) && role.getDeptIds() != null && !role.getDeptIds().isEmpty()) {
            List<SysRoleDept> list = new ArrayList<>();
            for (Long deptId : role.getDeptIds()) {
                list.add(new SysRoleDept(role.getId(), deptId));
            }
            // 批量插入（如果数据量大建议用 MyBatis Batch，这里简单循环或 saveBatch）
            // 这里演示简单的循环插入，实际建议在 Mapper 写 batchInsert
            // TODO: 2026/1/13 后续需要在此修改为savebatch 
            list.forEach(sysRoleDeptMapper::insert);
        }
    }

    @Override
    // 新增接口方法：查询角色拥有的部门ID（用于回显）
    public List<Long> getDeptIdsByRoleId(Long roleId) {
        return sysRoleDeptMapper.selectList(
                new LambdaQueryWrapper<SysRoleDept>().eq(SysRoleDept::getRoleId, roleId)
        ).stream().map(SysRoleDept::getDeptId).toList();
    }
}