package com.mogo.project.modules.system.controller;

import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.common.util.SecurityUtils;
import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.system.model.dto.ClientErrorReportDto;
import com.mogo.project.modules.system.model.entity.SysClientErrorLog;
import com.mogo.project.modules.system.service.ISysClientErrorLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "10. 前端异常日志")
@RestController
@RequestMapping("/monitor/client-error")
@RequiredArgsConstructor
public class SysClientErrorLogController {

    private final ISysClientErrorLogService service;

    @Anonymous
    @Operation(summary = "前端异常上报")
    @PostMapping("/report")
    public ApiResponse<Void> report(@Valid @RequestBody ClientErrorReportDto dto,
                                    HttpServletRequest request) {
        service.validateAndGuard(dto, request);

        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysClientErrorLog entity = service.buildEntity(dto, request, loginUser);
        service.recordAsync(entity);
        return ApiResponse.success();
    }


}