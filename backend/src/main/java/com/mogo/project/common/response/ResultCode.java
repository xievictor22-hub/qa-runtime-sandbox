package com.mogo.project.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * API 统一返回状态码
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILED(500, "系统内部错误"),

    // 认证授权相关
    UNAUTHORIZED(401, "暂未登录或token已经过期"),
    FORBIDDEN(403, "没有相关权限"),

    // 参数校验
    VALIDATE_FAILED(400, "参数检验失败"),

    // 业务常用
    USER_NOT_EXIST(5001, "用户不存在"),
    USER_PASSWORD_ERROR(5002, "用户名或密码错误");

    private final int code;
    private final String message;
}