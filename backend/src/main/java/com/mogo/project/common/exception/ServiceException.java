package com.mogo.project.common.exception;

import com.mogo.project.common.response.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 业务异常
 * 用于在 Service 层手动抛出，由 GlobalExceptionHandler 捕获并返回给前端
 */
@Getter
public final class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误提示
     */
    private final String message;

    /**
     * 使用默认 500 错误
     */
    public ServiceException(String message) {
        super(message);
        this.message = message;
        this.code = ResultCode.FAILED.getCode(); // 建议使用枚举的 code
    }

    /**
     * 使用自定义错误码
     */
    public ServiceException(String message, Integer code) {
        super(message);
        this.message = message;
        this.code = code;
    }
    public ServiceException( Integer code,String message) {
        super(message);
        this.message = message;
        this.code = code;
    }

    /**
     * 使用 ResultCode 枚举 (最推荐)
     */
    public ServiceException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.message = resultCode.getMessage();
        this.code = resultCode.getCode();
    }

    /**
     * 包装其他异常
     */
    public ServiceException(String message, Throwable e) {
        super(message, e);
        this.message = message;
        this.code = ResultCode.FAILED.getCode();
    }
}