package com.mogo.project.modules.auth.filter;



import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.auth.security.UserDetailsServiceImpl;
import com.mogo.project.modules.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;


    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws  IOException {
        try {
            // 使用 TokenService 获取 Redis 中的用户信息
            LoginUser loginUser = tokenService.getLoginUser(request);
                // 4. 如果上下文里没有用户，则加载用户并设置上下文
                if (loginUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // ★★★ 验证并自动刷新有效期 ★★★
                    tokenService.verifyToken(loginUser);
                    // 构建认证对象
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            loginUser, null, loginUser.getAuthorities());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 设置到 Security 上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
//            }

            chain.doFilter(request, response);
        }catch (io.jsonwebtoken.ExpiredJwtException e) {
            // ★★★ 捕获过期异常，手动返回 JSON 401 ★★★
            log.error("Token 已过期: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 401, \"message\": \"登录已过期，请重新登录\", \"success\": false}");
            // 不再调用 chain.doFilter，请求到此终止

        } catch (Exception e) {
            // 捕获其他解析错误（如签名错误、格式错误）
            log.error("Token 解析失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\": 401, \"message\": \"Token 无效\", \"success\": false}");
        }
    }

}