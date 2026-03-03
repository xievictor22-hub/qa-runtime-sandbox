package com.mogo.project.modules.system.controller;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mogo.project.common.annotation.Log;
import com.mogo.project.common.enums.BusinessType;
import com.mogo.project.common.excel.listener.DataImportListener;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.common.util.ExcelUtil;
import com.mogo.project.modules.system.model.entity.SysUser;
import com.mogo.project.modules.system.model.vo.UserExcelVO;
import com.mogo.project.modules.system.service.SysUserService;
import com.mogo.project.modules.system.model.dto.UserQueryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "01. 用户管理")
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('system:user:list')")
public class SysUserController {

    private final SysUserService sysUserService;
    @PreAuthorize("hasAuthority('system:user:query')")
    @Operation(summary = "分页获取用户列表")
    @GetMapping("/list")
    public ApiResponse<IPage<SysUser>> getUserList(UserQueryDto queryDto) {
        return ApiResponse.success(sysUserService.getUserPage(queryDto));
    }

    @PreAuthorize("hasAuthority('system:user:query')")
    @Operation(summary = "获取用户总列表")
    @GetMapping("/listAll")
    public ApiResponse<List<SysUser>> getAllUserList() {
        return ApiResponse.success(sysUserService.getAllUserList());
    }

    @PreAuthorize("hasAuthority('system:user:add')")
    //添加日志
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @Operation(summary = "新增用户")
    @PostMapping
    public ApiResponse<Boolean> addUser(@RequestBody SysUser sysUser) {
        return ApiResponse.success(sysUserService.saveUser(sysUser));
    }

    @PreAuthorize("hasAuthority('system:user:edit')")
    @Operation(summary = "修改用户")
    @PutMapping
    public ApiResponse<Boolean> updateUser(@RequestBody SysUser sysUser) {
        return ApiResponse.success(sysUserService.updateUser(sysUser));
    }
    @PreAuthorize("hasAuthority('system:user:remove')")
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteUser(@PathVariable Long id) {
        // 简单的逻辑删除
        return ApiResponse.success(sysUserService.removeById(id));
    }
    @PreAuthorize("hasAuthority('system:user:remove')")
    @Operation(summary = "批量删除用户")
    @DeleteMapping("/batch")
    public ApiResponse<Boolean> deleteBatchUser(@RequestBody List<Long> ids) {
        return ApiResponse.success(sysUserService.removeBatchByIds(ids));
    }
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Operation(summary = "获取用户的角色ID列表")
    @GetMapping("/{userId}/role")
    public ApiResponse<List<Long>> getUserRoleIds(@PathVariable Long userId) {
        return ApiResponse.success(sysUserService.getRoleIdsByUserId(userId));
    }
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Operation(summary = "给用户分配角色")
    @PutMapping("/{userId}/role")
    public ApiResponse<String> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        sysUserService.assignRoles(userId, roleIds);
        return ApiResponse.success("分配成功！");
    }

    /**
     * 导出接口
     */
    @PreAuthorize("hasAuthority('system:user:query')")
    @Operation(summary = "导出用户数据")
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        // 1. 获取数据 (实际场景可能带有查询条件)
        List<SysUser> userList = sysUserService.list();

        // 2. Entity 转 VO (使用 BeanUtil 或 MapStruct)
        // 这里假设你已经转换好了 List<UserExcelVO>
        List<UserExcelVO> exportList = BeanUtil.copyToList(userList, UserExcelVO.class);

        // 3. 调用工具类导出
        ExcelUtil.writeExcel(response, exportList, UserExcelVO.class, "用户列表", "user_data");
    }

    /**
     * 导入接口
     */
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Operation(summary = "导入用户数据")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> importData(@RequestPart("file") MultipartFile file) throws IOException {

        // 核心代码：一行搞定大数据量导入
        EasyExcel.read(file.getInputStream(), UserExcelVO.class, new DataImportListener<UserExcelVO>(dataList -> {
            // 这里是监听器回调回来的数据列表 (List<UserExcelVO>)
            // 1. VO 转 Entity
            List<SysUser> entities = BeanUtil.copyToList(dataList, SysUser.class);
            // 2. 调用 MyBatis Plus 的批量保存
            sysUserService.saveBatch(entities);
        })).sheet().doRead();

        return ApiResponse.success("导入成功");
    }

    @Operation(summary = "根据角色权限字符获取用户列表")
    @GetMapping("/listByRoleKey")
    public ApiResponse<List<SysUser>> listByRoleKey(@RequestParam String roleKey) {
        // 逻辑：关联 sys_user_role 和 sys_role 表查询
        // 这里演示 Service 调用，具体 SQL 需在 Mapper 实现
        List<SysUser> list = sysUserService.selectUserListByRoleKey(roleKey);
        return ApiResponse.success(list);
    }

}