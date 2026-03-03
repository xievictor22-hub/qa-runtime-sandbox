package com.mogo.project.modules.auth.security;

import cn.hutool.json.JSONUtil;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.common.response.ResultCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 自定义未登录或者token失效时的返回结果
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        // 使用 Hutool 将对象转为 JSON 字符串写入响应
        response.getWriter().println(JSONUtil.toJsonStr(ApiResponse.error(ResultCode.UNAUTHORIZED)));
        response.getWriter().flush();
    }
}