package com.mogo.project.modules.quote.controller.app;

import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.modules.quote.domain.process.dto.QuoteProcessDto;
import com.mogo.project.modules.quote.domain.process.service.IQuoteProcessService;
import com.mogo.project.modules.quote.domain.process.vo.QuoteProcessUserVO;
import com.mogo.project.modules.system.model.entity.SysUser;
import com.mogo.project.modules.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "报价单-流程流转")
@RestController
@RequestMapping("/quote/process") // -> quote:process
@RequiredArgsConstructor
@Validated
public class QuoteProcessController {

    private final IQuoteProcessService processService;

    private final SysUserService sysUserService;

    @PreAuthorize("hasAuthority('quote:process:submit')")
    @Operation(summary = "提交审核")
    @PostMapping("/submit")
    public ApiResponse<Void> submit(@Valid @RequestBody QuoteProcessDto dto) {
        if (dto.getNextHandlerId() == null) {
            throw new ServiceException("下一步处理人不能为空");
        }
        processService.submitAudit(dto.getId(), dto.getNextHandlerId(), dto.getAuditComment());
        return ApiResponse.success(null);
    }

    @PreAuthorize("hasAuthority('quote:process:audit-pass')")
    @Operation(summary = "审核通过")
    @PostMapping("/audit-pass")
    public ApiResponse<Void> auditPass(@Valid @RequestBody QuoteProcessDto dto) {
        if (dto.getNextHandlerId() == null) {
            throw new ServiceException("下一步处理人不能为空");
        }
        processService.auditPass(dto.getId(), dto.getNextHandlerId(), dto.getAuditComment());
        return ApiResponse.success(null);
    }

    @PreAuthorize("hasAuthority('quote:process:audit-reject')")
    @Operation(summary = "审核驳回")
    @PostMapping("/audit-reject")
    public ApiResponse<Void> auditReject(@Valid @RequestBody QuoteProcessDto dto) {
        processService.auditReject(dto.getId(), dto.getAuditComment());
        return ApiResponse.success(null);
    }

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
    @PreAuthorize("hasAnyAuthority('quote:process:submit','quote:process:audit-pass','quote:process:audit-reject','quote:process:new-version')")
    @GetMapping("/users/{roleKey}")
    public ApiResponse<List<QuoteProcessUserVO>> getProcessUsers(@PathVariable String roleKey) {
        List<SysUser> users = sysUserService.selectUserListByRoleKey(roleKey);
        List<QuoteProcessUserVO> result = users.stream().map(u -> {
            QuoteProcessUserVO vo = new QuoteProcessUserVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setNickname(u.getNickname());
            vo.setRoleNames(u.getRoleNames());
            return vo;
        }).collect(Collectors.toList());
        return ApiResponse.success(result);
    }

}
