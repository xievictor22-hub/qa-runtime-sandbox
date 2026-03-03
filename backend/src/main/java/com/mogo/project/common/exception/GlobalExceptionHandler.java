package com.mogo.project.common.exception;

import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.common.response.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理 (全场景覆盖版)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ================== 1. 核心业务异常 ==================

    /**
     * 处理自定义业务异常
     * 优化点：务必使用 e.getCode()，不要丢失业务层传递的状态码
     */
    @ExceptionHandler(ServiceException.class)
    public ApiResponse<Void> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.warn("业务异常: url:{}, code:{}, msg:{}", request.getRequestURI(), e.getCode(), e.getMessage());
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(ImportValidationException.class)
    public ApiResponse<String> handleImportValidationException(ImportValidationException e) {
        return ApiResponse.error(40001, e.getMessage(),e.getErrorUrl());
    }

    // ================== 2. 安全/权限异常 (Spring Security) ==================



    /**
     * 权限不足异常 (403)
     * 说明：Spring Security 抛出 AccessDeniedException 时，会被这里捕获
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Void> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        log.warn("权限不足: url:{}, msg:{}", request.getRequestURI(), e.getMessage());
        return ApiResponse.error(ResultCode.FORBIDDEN);
    }

    /**
     * 认证失败类异常 (虽然通常由 EntryPoint 处理，但部分场景可能抛出到 Controller)
     */
    @ExceptionHandler({BadCredentialsException.class, AccountExpiredException.class, LockedException.class, DisabledException.class})
    public ApiResponse<Void> handleAuthException(Exception e) {
        String msg = "认证失败";
        if (e instanceof BadCredentialsException) msg = "用户名或密码错误";
        else if (e instanceof LockedException) msg = "账户已被锁定";
        else if (e instanceof DisabledException) msg = "账户已被禁用";
        else if (e instanceof AccountExpiredException) msg = "账户已过期";

        return ApiResponse.error(ResultCode.UNAUTHORIZED.getCode(), msg);
    }

    // ================== 3. 参数校验异常 ==================

    /**
     * 处理 @RequestBody 参数校验异常 (JSON)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = formatBindingResult(e.getBindingResult());
        log.warn("参数校验失败(Body): {}", message);
        return ApiResponse.validateFailed(message);
    }

    /**
     * 处理 @ModelAttribute 参数校验异常 (Form表单)
     */
    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e) {
        String message = formatBindingResult(e.getBindingResult());
        log.warn("参数校验失败(Form): {}", message);
        return ApiResponse.validateFailed(message);
    }

    /**
     * 处理 @RequestParam / @PathVariable 单个参数校验异常
     * 例如: @RequestParam @Min(10) int age
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败(Single): {}", message);
        return ApiResponse.validateFailed(message);
    }

    // ================== 4. 数据库与系统级异常 ==================

    /**
     * 数据库唯一键冲突 (如: 用户名重复注册)
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据重复冲突: {}", e.getMessage());
        return ApiResponse.error("当前数据已存在，请勿重复操作");
    }

    /**
     * 请求方式不支持 (如 POST 接口用了 GET)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方式不支持: url:{}, method:{}", request.getRequestURI(), e.getMethod());
        return ApiResponse.error(405, "不支持当前请求方法: " + e.getMethod());
    }

    /**
     * 参数格式错误 (如 JSON 格式不对，或 int 字段传了 string)
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ApiResponse<Void> handleArgumentTypeMismatch(Exception e) {
        log.warn("参数格式错误: {}", e.getMessage());
        return ApiResponse.error(400, "请求参数格式错误，请检查输入类型");
    }

    /**
     * 404 接口不存在 (需要在 application.yml 开启 throw-exception-if-no-handler-found: true 才生效)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponse<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return ApiResponse.error(404, "接口不存在: " + e.getRequestURL());
    }

    // ================== 5. 兜底异常 ==================

    /**
     * 处理所有不可知的异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("系统未知异常:", e);
        // 生产环境建议返回 "系统内部异常"，避免将具体的 stack trace 暴露给前端
        return ApiResponse.error(ResultCode.FAILED.getCode(), "系统内部异常，请联系管理员");
    }

    /**
     * 辅助方法：格式化 BindingResult 错误信息
     */
    private String formatBindingResult(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                return fieldError.getDefaultMessage();
            }
        }
        return "参数校验失败";
    }
}