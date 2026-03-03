package com.mogo.project.common.aspect;

import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.modules.auth.model.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;

/**
 * 自动配置权限
 * POST 请求 /system/user -> 自动映射为权限 system:user:add
 * PUT 请求 /system/user -> 自动映射为权限 system:user:edit
 * DELETE 请求 /system/user/{id} -> 自动映射为权限 system:user:remove
 * GET 请求 /system/user/list -> 自动映射为权限 system:user:query
 */
@Slf4j
@Aspect
@Component
public class AutoPermissionAspect {

    @Before("execution(* com.mogo.project.modules..controller..*.*(..))")
    public void doBefore(JoinPoint point) {
        // 1. 如果方法或类上标记了 @Anonymous，直接放行
        if (point.getSignature().getDeclaringType().isAnnotationPresent(Anonymous.class)
                || ((org.aspectj.lang.reflect.MethodSignature) point.getSignature()).getMethod().isAnnotationPresent(Anonymous.class)) {
            return;
        }

        // 2. 获取 Request 和当前用户
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;
        HttpServletRequest request = attributes.getRequest();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            // 未登录由 Spring Security 处理，这里忽略
            return;
        }

        // 3. 解析 URL 和 Method 生成权限字符串
        // 假设 URL 是 /system/user/list 或 /system/user
        // 我们取前两段作为模块: system:user
        String requestURI = request.getRequestURI();
        String method = request.getMethod().toUpperCase();

        // 简单的解析逻辑 (你需要根据你的实际 URL 规范调整)
        // 去掉开头的 /
        String path = requestURI.substring(1);
        String[] parts = path.split("/");

        // 至少要有 system/user 这种结构
        if (parts.length < 2) return;

        String module = parts[0];
        String resource = parts[1];
        String action = "";

        // 4. 根据 HTTP 动作映射操作
        if (requestURI.endsWith("/list")) {
            action = "list"; // 特殊处理分页查询
        } else if ("POST".equals(method)) {
            action = "add";
        } else if ("PUT".equals(method)) {
            action = "edit";
        } else if ("DELETE".equals(method)) {
            action = "remove";
        } else if ("GET".equals(method)) {
            action = "query"; // 普通 GET 视为查询
        }

        if (action.isEmpty()) return;

        // 5. 拼装权限字符串: system:user:add
        String permission = String.format("%s:%s:%s", module, resource, action);
        // =========================================================================
        // ★★★ 新增：打印详细鉴权日志 ★★★
        // =========================================================================
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String username = loginUser.getUsername();
        // 获取当前用户的所有权限列表 (List<String>)
        List<String> userPermissions = loginUser.getPermissions();
//todo 先注释掉 权限日志
//        log.info("┌───────────────────────────────────────────────────────────");
//        log.info("│ [权限校验]");
//        log.info("│ ├─ 当前用户: {}", username);
//        log.info("│ ├─ 访问路径: {} [{}]", requestURI, method);
//        log.info("│ ├─ 目标权限: {}", permission);
//        log.info("│ └─ 用户权限: {}", userPermissions); // 这行最关键，能看出来缺什么
//        log.info("└───────────────────────────────────────────────────────────");

        // 6. 校验权限
        // 超级管理员直接放行
        if (loginUser.getSysUser().getId() == 1L) return;

        boolean hasPerm = authentication.getAuthorities().contains(new SimpleGrantedAuthority(permission));

        if (!hasPerm) {
            throw new AccessDeniedException("没有权限访问: " + permission);
        }
    }
}