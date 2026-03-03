package com.mogo.project.modules.system.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserOnlineDto implements Serializable {
    private String uuid;         // 会话ID (JWT中的唯一标识)
    private String token;        // 完整的 Token 字符串 (用于展示或匹配)
    private String username;     // 用户名
    private String ipaddr;       // IP地址
    private String loginLocation;// 登录地点 (可留空，需IP库支持)
    private String browser;      // 浏览器
    private String os;           // 操作系统
    private Long loginTime;      // 登录时间戳
}