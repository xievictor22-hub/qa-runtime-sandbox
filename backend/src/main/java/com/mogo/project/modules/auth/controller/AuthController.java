package com.mogo.project.modules.auth.controller;

import com.mogo.project.common.annotation.Anonymous;
import com.mogo.project.common.annotation.RateLimit;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.common.response.ApiResponse;
import com.mogo.project.modules.auth.dto.LoginDto;
import com.mogo.project.modules.auth.dto.RefreshRequest;
import com.mogo.project.modules.auth.dto.TokenResponse;
import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.auth.service.AuthService;
import com.mogo.project.modules.auth.service.TokenService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Tag(name = "00. 认证中心") // Swagger 分组名称
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Anonymous
public class AuthController {

    private final AuthService authService;

    private final TokenService tokenService; // 注入 TokenService
    private final RedisTemplate<String, Object> redisTemplate;

    // Token 有效期 (比如 30分钟)
    @Value("${jwt.access-token-expiration:1800000}")
    private long ACCESS_TOKEN_EXPIRATION;
    //刷新token有效期7天
    @Value("${jwt.refresh-token-expiration:604800000}")
    private long REFRESH_TOKEN_EXPIRATION;

    @RateLimit(count = 10, seconds = 60, message = "登录过于频繁，请1分钟后再试")
    @Anonymous
    @Operation(summary = "用户登录", description = "返回 Token")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody @Valid LoginDto loginDto) {
        Map<String, Object> result = authService.login(loginDto);
        return ApiResponse.success( result,"登录成功");
    }

    @RateLimit(count = 10, seconds = 60, message = "登录过于频繁，请1分钟后再试")
    @Anonymous
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        // 获取 Header 中的 Token
        String token = request.getHeader("Authorization");
        // 执行删除
        tokenService.deleteToken(token);
        return ApiResponse.success();
    }

    @RateLimit(count = 10, seconds = 60, message = "登录过于频繁，请1分钟后再试")
    @Anonymous
    @Operation(summary = "token刷新", description = "返回 Token")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody RefreshRequest req) {
        String refreshToken = req.getRefreshToken();
        if (!StringUtils.hasText(refreshToken)) {
            return ApiResponse.error(401,"refreshToken不能为空");
        }
        Claims c ;
        try {
            c = tokenService.parseToken(refreshToken);
        } catch (Exception e) {
            return ApiResponse.error(401,"refreshToken无效或已过期");
        }
        String uuid = (String)c.get("login_user_key");
        String tokenKey = tokenService.getTokenKey(uuid);
        LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(tokenKey);
        if(loginUser == null) {
            return ApiResponse.error(401,"会话不存在或已过期，请重新登录");
        }
        String jti = (String)c.get("jti");
        String deviceId = (String)c.get("device_id");
        String key = tokenService.getRefreshJtiKey(uuid);
        String currentJti = (String)redisTemplate.opsForValue().get(key);


        if(currentJti==null)return ApiResponse.error(401,"会话已失效，请重新登录");
        if(!currentJti.equals(jti)){
            //盗用，重放
            redisTemplate.delete(tokenKey);//删除登录信息
            redisTemplate.delete(key);//删除刷新信息
            redisTemplate.delete(tokenService.getUserLastTokenKey(c.getSubject(),deviceId));
            return ApiResponse.error(401,"refreshToken已失效（疑似盗用），请重新登录");
        }
        //rotation 刷新refreshToken
        String newJti = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(key,newJti,REFRESH_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);
        Map<String,Object> accessClaims  = new  HashMap<>();
        accessClaims.put("login_user_key", uuid);
        if(StringUtils.hasText(deviceId)){
            accessClaims.put("device_id",deviceId);
        }
        Map<String,Object> refreshClaims = new HashMap<>(accessClaims);
        refreshClaims.put("jti", newJti);
        String newAccess = tokenService.createJwt(accessClaims, c.getSubject(), ACCESS_TOKEN_EXPIRATION);
        String newRefresh = tokenService.createJwt(refreshClaims, c.getSubject(), REFRESH_TOKEN_EXPIRATION);
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setRefreshToken(newRefresh);
        tokenResponse.setAccessToken(newAccess);
        // /auth/refresh 允许匿名访问，SecurityContext 中 principal 可能是 "anonymousUser"（String）
        // 这里应使用已通过 refreshToken + Redis 校验过的 loginUser 续期
        tokenService.refreshToken(loginUser);
        return ApiResponse.success(tokenResponse);
    }
}
