package com.mogo.project.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mogo.project.common.annotation.Log;
import com.mogo.project.common.enums.BusinessType;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.system.model.dto.LoginInforQueryDto;
import com.mogo.project.modules.system.model.entity.SysLoginInfor;
import com.mogo.project.modules.system.service.SysLoginInforService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "04. 操作日志")
@RestController
@RequestMapping("/monitor/logininfor") // 归类到 monitor 模块
@RequiredArgsConstructor
public class SysLoginInforController {

    private final SysLoginInforService sysLoginInforService;

    @Operation(summary = "分页列表")
    @PreAuthorize("hasAuthority('monitor:logininfor:list')")
    @GetMapping("/list")
    public ApiResponse<IPage<SysLoginInfor>> list(LoginInforQueryDto dto) {
        return ApiResponse.success(sysLoginInforService.selectPage(dto));
    }

    @Operation(summary = "批量删除")
    @PreAuthorize("hasAuthority('monitor:logininfor:remove')")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{loginIds}")
    public ApiResponse<Void> remove(@PathVariable List<Long> loginIds) {
        sysLoginInforService.deleteLoginInforByIds(loginIds);
        return ApiResponse.success();
    }

    @Operation(summary = "清空日志")
    @PreAuthorize("hasAuthority('monitor:logininfor:remove')")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public ApiResponse<Void> clean() {
        sysLoginInforService.cleanLoginInfor();
        return ApiResponse.success();
    }
}