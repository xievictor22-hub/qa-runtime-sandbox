package com.mogo.project.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.slf4j.MDC;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用返回对象
 * 优化点：
 * 1. 加入 Swagger/SpringDoc 注解 (@Schema)
 * 2. 加入 requestId 链路追踪 ID
 * 3. 加入 success 布尔值辅助判断
 * 4. 序列化时忽略 null 字段，减少传输体积
 *
 * @param <T>
 */
@Data
@Accessors(chain = true) // 开启链式调用
@Schema(description = "统一响应结构体")
@JsonInclude(JsonInclude.Include.NON_NULL) // data 为 null 时不返回该字段
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "是否成功", example = "true")
    private boolean success;

    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    @Schema(description = "承载数据")
    private T data;

    @Schema(description = "响应时间戳", example = "1698765432100")
    private long timestamp;

    @Schema(description = "请求ID (链路追踪)", example = "a1b2c3d4")
    private String requestId;

    protected ApiResponse() {
    }

    protected ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
        // 自动计算 success 状态，前端少写一个判断逻辑
        this.success = ResultCode.SUCCESS.getCode() == code;
        // 获取当前请求的 TraceId (需要配合日志拦截器将 TraceId 放入 MDC)
        this.requestId = MDC.get("traceId");
    }

    // ================== 成功响应 ==================

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ================== 失败响应 ==================

    public static <T> ApiResponse<T> error(ResultCode errorCode) {
        return new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 业务异常返回 (使用默认错误码，自定义消息)
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(ResultCode.FAILED.getCode(), message, null);
    }

    /**
     * 自定义错误码和消息和数据
     */
    public static <T> ApiResponse<T> error(Integer code, String message,T data) {
        return new ApiResponse<>(code, message, data);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<T>(code, message, null);
    }

    public static <T> ApiResponse<T> validateFailed(String message) {
        return new ApiResponse<>(ResultCode.VALIDATE_FAILED.getCode(), message, null);
    }
}