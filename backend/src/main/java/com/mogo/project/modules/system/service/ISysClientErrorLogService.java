package com.mogo.project.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.system.model.dto.ClientErrorReportDto;
import com.mogo.project.modules.system.model.entity.SysClientErrorLog;
import jakarta.servlet.http.HttpServletRequest;

public interface ISysClientErrorLogService extends IService<SysClientErrorLog> {
    void validateAndGuard(ClientErrorReportDto dto, HttpServletRequest request);
    void archiveAndCleanup();
    void recordAsync(SysClientErrorLog sysClientErrorLog);
    SysClientErrorLog buildEntity(ClientErrorReportDto dto, HttpServletRequest req, LoginUser loginUser);
}
