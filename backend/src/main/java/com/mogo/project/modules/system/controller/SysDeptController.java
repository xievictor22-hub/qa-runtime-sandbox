package com.mogo.project.modules.system.controller;

import com.mogo.project.common.annotation.Log;
import com.mogo.project.common.enums.BusinessType;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.system.model.entity.SysDept;
import com.mogo.project.modules.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Tag(name = "07. 部门管理")
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService sysDeptService;

    @Operation(summary = "获取部门列表")
    @PreAuthorize("hasAuthority('system:dept:list')")
    @GetMapping("/list")
    public ApiResponse<List<SysDept>> list(SysDept dept) {
        return ApiResponse.success(sysDeptService.getDeptTree(dept));
    }

    @Operation(summary = "新增部门")
    @PreAuthorize("hasAuthority('system:dept:add')")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    public ApiResponse<Void> add(@RequestBody SysDept dept) {
        if (sysDeptService.checkDeptNameUnique(dept)) {
            return ApiResponse.error("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        sysDeptService.insertDept(dept);
        return ApiResponse.success();
    }

    @Operation(summary = "修改部门")
    @PreAuthorize("hasAuthority('system:dept:edit')")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public ApiResponse<Void> edit(@RequestBody SysDept dept) {
        if (sysDeptService.checkDeptNameUnique(dept)) {
            return ApiResponse.error("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        sysDeptService.updateDept(dept);
        return ApiResponse.success();
    }

    @Operation(summary = "删除部门")
    @PreAuthorize("hasAuthority('system:dept:remove')")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public ApiResponse<Void> remove(@PathVariable Long deptId) {
        if (sysDeptService.hasChildByDeptId(deptId)) {
            return ApiResponse.error("存在下级部门,不允许删除");
        }
        if (sysDeptService.checkDeptExistUser(deptId)) {
            return ApiResponse.error("部门存在用户,不允许删除");
        }
        sysDeptService.removeById(deptId);
        return ApiResponse.success();
    }

}