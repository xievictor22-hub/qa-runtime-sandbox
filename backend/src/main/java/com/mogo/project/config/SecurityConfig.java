package com.mogo.project.config;

import com.mogo.project.modules.auth.filter.JwtAuthenticationTokenFilter;
import com.mogo.project.modules.auth.security.RestAuthenticationEntryPoint;
import com.mogo.project.modules.auth.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启注解权限控制
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    /**
     * 1. 过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (前后端分离不需要)
                .csrf(AbstractHttpConfigurer::disable)

                // 设置 Session 策略为无状态 (因为我们用 JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置异常处理 (401 返回 JSON)
                .exceptionHandling(handling -> handling.authenticationEntryPoint(restAuthenticationEntryPoint))

                // 配置请求拦截规则
                .authorizeHttpRequests(auth -> auth
                        // 允许登录、注册接口匿名访问
                        .requestMatchers("/auth/login","/auth/refresh").permitAll()
                        // 允许 Swagger 文档匿名访问
                        .requestMatchers("/static/**","/doc.html","/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // 允许静态资源
                        .requestMatchers("/", "/*.html", "/*.css", "/*.js").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )

                // 配置 AuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // 添加 JWT 过滤器 (在 UsernamePasswordAuthenticationFilter 之前)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    /**
     * ⚠️ 新增配置：静态资源暴力放行
     * 这些路径将完全绕过 Spring Security 过滤器链
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/doc.html",              // Knife4j 入口
                "/webjars/**",            // Knife4j 静态资源 (CSS/JS)
                "/swagger-resources/**",  // Swagger 资源
                "/v3/api-docs/**",        // 接口定义数据
                "/favicon.ico"            // 图标
        );
    }

    /**
     * 2. 认证管理器 (登录接口需要用到)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 3. 认证提供者 (关联 UserDetailService 和 PasswordEncoder)
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 4. 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}