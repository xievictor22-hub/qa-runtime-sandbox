package com.mogo.project.modules.quote.controller.app;

import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.annotation.Log;
import com.mogo.project.common.enums.BusinessType;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.common.util.SecurityUtils;
import com.mogo.project.modules.quote.domain.process.dto.QuoteProcessDto;
import com.mogo.project.modules.quote.domain.process.service.IQuoteProcessService;
import com.mogo.project.modules.system.model.entity.SysUser;
import com.mogo.project.modules.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag(name = "报价单-流程流转")
@RestController
@RequestMapping("/quote/process") // -> quote:process
@RequiredArgsConstructor
@Validated
public class QuoteProcessController {

    private final IQuoteProcessService processService;

    private final SysUserService sysUserService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Anonymous
    @PreAuthorize("hasAuthority('quote:process:submit')")
    @Operation(summary = "提交审核")
    @Log(title = "操作日志", businessType = BusinessType.UPDATE)
    @PostMapping("/submit")
    public ApiResponse<Void> submit(@RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
                                    @Valid @RequestBody QuoteProcessDto dto) {
        guardIdempotent("submit", dto.getId(), idempotencyKey);
        if (dto.getNextHandlerId() == null) {
            throw new ServiceException("下一步处理人不能为空");
        }
        processService.submitAudit(dto.getId(), dto.getNextHandlerId(), dto.getAuditComment());
        return ApiResponse.success(null);
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:process:audit-pass')")
    @Operation(summary = "审核通过")
    @Log(title = "审核通过", businessType = BusinessType.UPDATE)
    @PostMapping("/audit-pass")
    public ApiResponse<Void> auditPass(@Valid @RequestBody QuoteProcessDto dto,
                                       @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        guardIdempotent("audit-pass", dto.getId(), idempotencyKey);
        if (dto.getNextHandlerId() == null) {
            throw new ServiceException("下一步处理人不能为空");
        }
        processService.auditPass(dto.getId(), dto.getNextHandlerId(), dto.getAuditComment());
        return ApiResponse.success(null);
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:process:audit-reject')")
    @Operation(summary = "审核驳回")
    @Log(title = "审核驳回", businessType = BusinessType.UPDATE)
    @PostMapping("/audit-reject")
    public ApiResponse<Void> auditReject(@RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
                                         @Valid @RequestBody QuoteProcessDto dto) {
        guardIdempotent("audit-reject", dto.getId(), idempotencyKey);
        processService.auditReject(dto.getId(), dto.getAuditComment());
        return ApiResponse.success(null);
    }

    @Anonymous
    @PreAuthorize("hasAuthority('quote:process:new-version')")
    @Operation(summary = "创建新报价版本")
    @Log(title = "创建新报价版本", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/new-version")
    public ApiResponse<Void> createNewVersion(@PathVariable Long id,
                                              @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey){
        guardIdempotent("new-version", id, idempotencyKey);
        processService.createNewQuoteVersion(id);
        return ApiResponse.success(null);
    }

    /**
     * 获取候选处理人列表
     * @param roleKey 角色标识 (如: auditor, quoter, business)
     */
    @Anonymous
    @PreAuthorize("hasAnyAuthority('quote:process:submit','quote:process:audit-pass','quote:process:audit-reject','quote:process:new-version')")
    @GetMapping("/users/{roleKey}")
    public ApiResponse<List<SysUser>> getProcessUsers(@PathVariable String roleKey) {
        // 这里调用系统模块的方法，根据角色Key查询用户
        // 如果你的系统没有直接的方法，可以用 userService.selectUserList(new SysUser()) 配合角色过滤
        // 示例逻辑：
        return ApiResponse.success(sysUserService.selectUserListByRoleKey(roleKey));
    }

    /**
     * 幂等保护（可选）：前端传 X-Idempotency-Key 时生效，防止短时间重复点击。
     */
    private void guardIdempotent(String action, Long quoteId, String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        String redisKey = "quote:process:idempotent:" + SecurityUtils.getUserId() + ":" + action + ":" + quoteId + ":" + key;
        Boolean first = redisTemplate.opsForValue().setIfAbsent(redisKey, 1, 15, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(first)) {
            throw new ServiceException(409, "请求重复提交，请稍后重试");
        }
    }

}