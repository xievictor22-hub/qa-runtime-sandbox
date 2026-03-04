package com.mogo.project.modules.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

//前端错误日志接收类
@Data
public class ClientErrorReportDto {

    @NotBlank
    @Size(max = 64)
    private String appName;

    @NotBlank
    @Size(max = 32)
    private String env;

    @NotBlank
    @Size(max = 16)
    private String level;

    @NotBlank
    @Size(max = 64)
    private String errorType;

    @NotBlank
    @Size(max = 1000)
    private String message;

    private String stack;

    @Size(max = 1000)
    private String pageUrl;

    @Size(max = 255)
    private String routePath;

    @Size(max = 128)
    private String deviceId;

    @Size(max = 64)
    private String traceId;

    /**
     * 前端透传附加上下文（JSON 字符串）
     */
    private String extra;

    @NotNull
    private Long occurTime;

    /**
     * 鉴权字段：应用访问 Key
     */
    @NotBlank
    @Size(max = 64)
    private String appKey;

    /**
     * 鉴权字段：Unix 时间戳（秒）
     */
    @NotNull
    private Long timestamp;

    /**
     * 鉴权字段：签名（hex）
     */
    @NotBlank
    @Size(max = 128)
    private String sign;
}
