package com.mogo.project.common.util;

import cn.hutool.core.util.StrUtil;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.modules.auth.model.LoginUser; // 注意：这是你的 UserDetails 实现类
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

/**
 * 安全服务工具类
 */
public class SecurityUtils {

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        try {
            return getLoginUser().getSysUser().getId();
        } catch (Exception e) {
            throw new ServiceException("获取用户ID异常", 401);
        }
    }

    /**
     * 获取部门ID
     */
    public static Long getDeptId() {
        try {
            return getLoginUser().getSysUser().getDeptId();
        } catch (Exception e) {
            throw new ServiceException("获取部门ID异常", 401);
        }
    }

    /**
     * 获取用户账户
     */
    public static String getUsername() {
        try {
            return getLoginUser().getUsername();
        } catch (Exception e) {
            throw new ServiceException("获取用户账户异常", 401);
        }
    }

    /**
     * 获取用户完整信息 (LoginUser)
     */
    public static LoginUser getLoginUser() {
        try {
            // 从 SecurityContextHolder 中获取 Authentication
            Authentication authentication = getAuthentication();
            if (authentication == null) {
                throw new ServiceException("登录状态已过期", 401);
            }

            // 获取 Principal 并强转为我们自定义的 LoginUser
            Object principal = authentication.getPrincipal();
            if (principal instanceof LoginUser) {
                return (LoginUser) principal;
            }
            throw new ServiceException("无效的用户信息");
        } catch (Exception e) {
            throw new ServiceException("获取用户信息异常", 401);
        }
    }

    /**
     * 获取Authentication
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin() {
        Long userId = SecurityUtils.getUserId();
        return userId != null && 1L == userId;
    }

    public static boolean hasPermission(String permission) {
        if (StrUtil.isBlank(permission)) {
            return true; // 权限为空，说明是公开选项
        }
        // 获取当前登录用户的所有权限列表
        List<String> permissions = getLoginUser().getPermissions();
        if (permissions == null) return false;

        // 这里的逻辑取决于你怎么存权限的，通常是在 Authorities 里
        return  permissions.contains(permission);
    }
}