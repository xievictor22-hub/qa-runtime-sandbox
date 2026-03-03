package com.mogo.project.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mogo.project.common.annotation.Log;
import com.mogo.project.common.enums.BusinessType;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.system.model.dto.OperLogQueryDto;
import com.mogo.project.modules.system.model.entity.SysOperLog;
import com.mogo.project.modules.system.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "04. 操作日志")
@RestController
@RequestMapping("/monitor/operlog") // 归类到 monitor 模块
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogService sysOperLogService;

    @Operation(summary = "分页列表")
    @PreAuthorize("hasAuthority('monitor:operlog:list')")
    @GetMapping("/list")
    public ApiResponse<IPage<SysOperLog>> list(OperLogQueryDto dto) {
        return ApiResponse.success(sysOperLogService.selectOperLogPage(dto));
    }

    @Operation(summary = "批量删除")
    @PreAuthorize("hasAuthority('monitor:operlog:remove')")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{operIds}")
    public ApiResponse<Void> remove(@PathVariable List<Long> operIds) {
        sysOperLogService.deleteOperLogByIds(operIds);
        return ApiResponse.success();
    }

    @Operation(summary = "清空日志")
    @PreAuthorize("hasAuthority('monitor:operlog:remove')")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public ApiResponse<Void> clean() {
        sysOperLogService.cleanOperLog();
        return ApiResponse.success();
    }
}