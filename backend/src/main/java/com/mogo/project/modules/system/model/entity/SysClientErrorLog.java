package com.mogo.project.modules.system.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

//前端错误日志实体类
@Data
@TableName("sys_client_error_log")
public class SysClientErrorLog {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String appName;
    private String env;
    private String level;
    private String errorType;
    private String message;
    private String stack;
    private String pageUrl;
    private String routePath;
    private Long userId;
    private String username;
    private String ip;
    private String userAgent;
    private String deviceId;
    private String traceId;
    private String extraJson;
    private LocalDateTime occurTime;
    private LocalDateTime createdAt;
}
