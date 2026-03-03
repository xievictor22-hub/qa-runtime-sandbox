package com.mogo.project.modules.system.controller;

import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.system.service.IDingDriveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "钉盘管理")
@RestController
@RequestMapping("/system/ding-drive")
@RequiredArgsConstructor
public class DingDriveController {

    private final IDingDriveService dingDriveService;

    @PreAuthorize("hasAuthority('system:ding-drive:upload')")
    @Operation(summary = "上传文件到钉盘")
    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
        // 1. 调用 Service
        // 这里的异常（如空间不足、网络错误）会被 Service 抛出，
        // 并被 GlobalExceptionHandler 捕获，前端会收到标准的 500 错误提示。
        String fileId = dingDriveService.uploadFile(file);

        // 2. 成功返回文件ID
        return ApiResponse.success(fileId);
    }
}