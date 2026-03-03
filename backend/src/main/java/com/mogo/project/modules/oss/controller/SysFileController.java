package com.mogo.project.modules.oss.controller;

import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.oss.service.ISysFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "05. 文件服务")
@Anonymous // 根据需要决定是否匿名
@RestController
@RequestMapping("/oss/file")
@RequiredArgsConstructor
public class SysFileController {

    private final ISysFileService sysFileService;

    @Operation(summary = "通用文件上传")
    @PostMapping("/upload")
    public ApiResponse<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        // 调用 Service 获取 URL
        String url = sysFileService.upload(file);

        // 封装返回结果 (保持原有返回结构，防止前端报错)
        Map<String, String> result = new HashMap<>();
        result.put("name", file.getOriginalFilename());
        result.put("url", url);

        return ApiResponse.success(result);
    }
}