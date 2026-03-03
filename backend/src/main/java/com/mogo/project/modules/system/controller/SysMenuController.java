package com.mogo.project.modules.system.controller;

import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.common.util.SecurityUtils;
import com.mogo.project.modules.system.model.entity.SysMenu;
import com.mogo.project.modules.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "03. 菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @Anonymous
    @Operation(summary = "获取菜单树列表")
    @GetMapping("/list")
    public ApiResponse<List<SysMenu>> getMenuList(@RequestParam(required = false) String menuName,
                                                  @RequestParam(required = false) Integer status) {
        return ApiResponse.success(sysMenuService.getMenuTree(menuName, status));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    public ApiResponse<Boolean> addMenu(@RequestBody SysMenu menu) {
        if (sysMenuService.checkMenuNameUnique(menu)) {
            return ApiResponse.error("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        }
        return ApiResponse.success(sysMenuService.save(menu));
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    public ApiResponse<Boolean> updateMenu(@RequestBody SysMenu menu) {
        if (sysMenuService.checkMenuNameUnique(menu)) {
            return ApiResponse.error("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        }
        return ApiResponse.success(sysMenuService.updateById(menu));
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{menuId}")
    public ApiResponse<Boolean> remove(@PathVariable Long menuId) {
        if (sysMenuService.hasChildByMenuId(menuId)) {
            return ApiResponse.error("存在子菜单,不允许删除");
        }
        return ApiResponse.success(sysMenuService.removeById(menuId));
    }
    @Anonymous
    @Operation(summary = "获取动态路由 (当前用户的菜单)")
    @GetMapping("/getRouters")
    public ApiResponse<List<SysMenu>> getRouters() {
        // 获取当前登录用户 ID
        // 这里假设你已经有了 SecurityContextHolder 的工具类，或者直接从 Auth 获取
        // 为了演示，这里先从 SecurityContext 获取
        Long userId = SecurityUtils.getUserId();
//        (com.mogo.project.modules.auth.model.LoginUser)
//                org.springframework.security.core.context.SecurityContextHolder
//                        .getContext().getAuthentication().getPrincipal()).getSysUser().getId();

        return ApiResponse.success(sysMenuService.selectMenuTreeByUserId(userId));
    }
}