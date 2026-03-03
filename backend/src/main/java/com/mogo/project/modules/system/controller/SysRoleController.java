package com.mogo.project.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.system.model.dto.RoleQueryDto;
import com.mogo.project.modules.system.model.entity.SysRole;
import com.mogo.project.modules.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "02. 角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @Operation(summary = "分页列表")
    @GetMapping("/list")
    public ApiResponse<IPage<SysRole>> getRoleList(RoleQueryDto queryDto) {
        return ApiResponse.success(sysRoleService.getRolePage(queryDto));
    }

    @Operation(summary = "获取所有角色(下拉选使用)")
    @GetMapping("/all")
    public ApiResponse<List<SysRole>> getAllRoles() {
        return ApiResponse.success(sysRoleService.getAllRoles());
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public ApiResponse<Boolean> addRole(@RequestBody SysRole role) {
        return ApiResponse.success(sysRoleService.saveRole(role));
    }

    @Operation(summary = "修改角色")
    @PutMapping
    public ApiResponse<Boolean> updateRole(@RequestBody SysRole role) {
        return ApiResponse.success(sysRoleService.updateRole(role));
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteRole(@PathVariable Long id) {
        return ApiResponse.success(sysRoleService.deleteRole(id));
    }
    @Operation(summary = "获取角色的菜单ID集合")
    @GetMapping("/{roleId}/menu")
    public ApiResponse<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        return ApiResponse.success(sysRoleService.getMenuIdsByRoleId(roleId));
    }

    @Operation(summary = "给角色分配菜单权限")
    @PutMapping("/{roleId}/menu")
    public ApiResponse<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        sysRoleService.assignMenus(roleId, menuIds);
        return ApiResponse.success();
    }

    // SysRoleController.java
    @Operation(summary = "回显权限-部门信息")
    @GetMapping("/deptIds/{roleId}")
    public ApiResponse<List<Long>> getRoleDeptIds(@PathVariable Long roleId) {
        List<Long> deptIds = sysRoleService.getDeptIdsByRoleId(roleId);
        return ApiResponse.success(deptIds);
    }
}