package com.mogo.project.modules.quote.controller.app;

import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.quote.domain.process.dto.QuoteProcessDto;
import com.mogo.project.modules.quote.domain.process.service.IQuoteProcessService;
import com.mogo.project.modules.system.model.entity.SysUser;
import com.mogo.project.modules.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "报价单-流程流转")
@RestController
@RequestMapping("/quote/process") // -> quote:process
@RequiredArgsConstructor
public class QuoteProcessController {

    private final IQuoteProcessService processService;

    private final SysUserService sysUserService;

    @Anonymous
    @PreAuthorize("hasAuthority('quote:process:submit')")
    @Operation(summary = "提交审核")
    @PostMapping("/submit")
    public ApiResponse<Void> submit(@RequestBody QuoteProcessDto dto) {
        processService.submitAudit(dto.getId(), dto.getNextHandlerId(), dto.getAuditComment());
        return ApiResponse.success(null);
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:process:audit-pass')")
    @Operation(summary = "审核通过")
    @PostMapping("/audit-pass")
    public ApiResponse<Void> auditPass(@RequestBody QuoteProcessDto dto) {
        processService.auditPass(dto.getId(), dto.getNextHandlerId(), dto.getAuditComment());
        return ApiResponse.success(null);
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:process:audit-reject')")
    @Operation(summary = "审核驳回")
    @PostMapping("/audit-reject")
    public ApiResponse<Void> auditReject(@RequestBody QuoteProcessDto dto) {
        processService.auditReject(dto.getId(), dto.getAuditComment());
        return ApiResponse.success(null);
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:process:new-version')")
    @Operation(summary = "创建新报价版本")
    @PostMapping("/{id}/new-version")
    public ApiResponse<Void> createNewVersion(@PathVariable Long id) {
        processService.createNewQuoteVersion(id);
        return ApiResponse.success(null);
    }

    /**
     * 获取候选处理人列表
     * @param roleKey 角色标识 (如: auditor, quoter, business)
     */
    @Anonymous
    @GetMapping("/users/{roleKey}")
    public ApiResponse<List<SysUser>> getProcessUsers(@PathVariable String roleKey) {
        // 这里调用系统模块的方法，根据角色Key查询用户
        // 如果你的系统没有直接的方法，可以用 userService.selectUserList(new SysUser()) 配合角色过滤
        // 示例逻辑：
        return ApiResponse.success(sysUserService.selectUserListByRoleKey(roleKey));
    }

}