package com.mogo.project.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 负责 Token 的生成、解析和校验
 */
@Slf4j
@Component
public class JwtUtils {

    // 从 application.yml 读取配置，如果没有则使用默认值
    @Value("${jwt.secret:MogoProjectSecretKeyForJwtSigningShouldBeLongEnough2025}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认 24 小时 (毫秒)
    private long expiration;

    /**
     * 生成 Token
     * @param username 用户名
     * @return 加密后的 Token 字符串
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    /**
     * 生成 Token (私有方法)
     */
    private String createToken(Map<String, Object> claims, String subject) {
        // 使用 HMAC-SHA 算法签名
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // 设置主题（通常是用户名或ID）
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }
    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        final String tokenUsername = getUsernameFromToken(token);
        return  !isTokenExpired(token);
    }

    /**
     * 判断 Token 是否过期
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * 解析 Token 获取 Claims
     */
    private Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}