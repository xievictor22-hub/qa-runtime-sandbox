package com.mogo.project.modules.auth.service;

import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.system.mapper.SysMenuMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    // Token 有效期 (比如 30分钟)
    @Value("${jwt.access-token-expiration:1800000}")
    private long ACCESS_TOKEN_EXPIRATION;
    //刷新token有效期7天
    @Value("${jwt.refresh-token-expiration:604800000}")
    private long REFRESH_TOKEN_EXPIRATION;

    // Redis 缓存有效期 (比如 7天，允许用户7天内免登录，或者做自动刷新)
    private final static long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;
    private final static long EXPIRE_TIME_REDIS = 7 * 24 * 60L; // 7天 (分钟)
    private final static String USER_LAST_TOKEN_KEY = "user_last_token_key:";


    private final RedisTemplate<String, Object> redisTemplate;

    // 注入 SysMenuMapper 以便重新查权限
    private final SysMenuMapper sysMenuMapper;

    /**
     * 创建令牌
     */
    public Map<String, Object> createToken(LoginUser loginUser,String deviceId) {
        if(!StringUtils.hasText(deviceId)){
            // 兼容：老客户端没传 deviceId 的情况，给个固定值（会退化成“同端互踢，同账号也互踢”）
            deviceId = "unKnown";
        }
        String username = loginUser.getUsername();

        // ✅ 1) 按 “用户名 + 设备” 找旧会话
        String lastKey = getUserLastTokenKey(username, deviceId);
        String oldUuid = (String) redisTemplate.opsForValue().get(lastKey);

        if (StringUtils.hasText(oldUuid)) {
            // ✅ 2) 删除旧登录态（立刻失效）
            redisTemplate.delete(getTokenKey(oldUuid));        // login_tokens:oldUuid
            redisTemplate.delete(getRefreshJtiKey(oldUuid));   // refresh_jti:oldUuid
        }
        // ✅ 3) 创建新会话
        String uuid = UUID.randomUUID().toString(); // 生成唯一标识
        loginUser.setToken(uuid); // 将 UUID 存入 LoginUser (需在 LoginUser 加字段)
        refreshToken(loginUser); // 写Redis login_tokens:uuid

        //生成 refresh jti，写 refresh_jti:{uuid}
        String refreshJti = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("refresh_jti:"+uuid, refreshJti, REFRESH_TOKEN_EXPIRATION, TimeUnit.MILLISECONDS);

        //存储最新的用户名与redis中token key的对应关系,失效与redis令牌时效一致
        // ✅ 5) 写 lastKey（只影响同设备）
        redisTemplate.opsForValue().set(lastKey, uuid, EXPIRE_TIME_REDIS, TimeUnit.MINUTES);
        Map<String, Object> accessClaims  = new HashMap<>();
        accessClaims.put("login_user_key", uuid); // 放入 JWT Payload
        accessClaims.put("device_id", deviceId);
        // refresh token 额外带 jti
        HashMap<String, Object> refreshClaims = new HashMap<>(accessClaims );
        refreshClaims.put("jti", refreshJti);
        String accessToken = createJwt(accessClaims , username,ACCESS_TOKEN_EXPIRATION);
        String refreshToken = createJwt(refreshClaims, username,REFRESH_TOKEN_EXPIRATION);
        Map<String, Object> result = new HashMap<>();
        result.put("token", accessToken);
        result.put("refresh_token", refreshToken);
        result.put("uuid", uuid);
        return result;
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     */
    public void verifyToken(LoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if(currentTime > expireTime) {
            //已过期，不续命，交给刷新token处理
            return;
        }
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
            refreshToken(loginUser);
        }
    }

    /**
     * ★★★ 核心实现：刷新特定用户的缓存 ★★★刷新token权限
     */
    public void refreshUserCache(String username) {
        // 1. 查找该用户当前有效的 Token UUID
        String uuid = (String) redisTemplate.opsForValue().get(USER_LAST_TOKEN_KEY + username);

        if (StringUtils.hasText(uuid)) {
            String userKey = getTokenKey(uuid);

            // 2. 获取 Redis 中的旧 LoginUser
            LoginUser loginUser = (LoginUser) redisTemplate.opsForValue().get(userKey);

            if (loginUser != null) {
                // 3. 重新查询数据库中的最新权限
                List<String> newPermissions = sysMenuMapper.selectPermsByUserId(loginUser.getSysUser().getId());

                // 4. 更新 LoginUser 对象
                loginUser.setPermissions(newPermissions);

                // 5. 写回 Redis (保持原有的过期时间)
                // 获取剩余过期时间
                Long expire = redisTemplate.getExpire(userKey, TimeUnit.SECONDS);
                if (expire != null && expire > 0) {
                    redisTemplate.opsForValue().set(userKey, loginUser, expire, TimeUnit.SECONDS);
                }
            }
        }
    }

    /**
     * 刷新令牌有效期 (续期 Redis)
     */
    public void refreshToken(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + ACCESS_TOKEN_EXPIRATION ); // 注意单位转换

        // 缓存 Key: login_tokens:uuid
        String userKey = getTokenKey(loginUser.getToken());

        // 转换为 UserOnlineDto 存储 (为了监控列表展示方便，只存必要字段)
        // 或者直接存 LoginUser 对象也可以，看你是否需要反序列化出完整的 UserDetails
        // 这里演示存 LoginUser，方便 Filter 获取权限
        redisTemplate.opsForValue().set(userKey, loginUser, EXPIRE_TIME_REDIS, TimeUnit.MINUTES);
    }

    /**
     * 获取登录用户 (从 Redis)
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        // 获取请求携带的令牌
        String token = getHeaderToken(request);
        if (StringUtils.hasText(token)) {
            try {
                Claims claims = parseToken(token);
                // 解析对应的 UUID
                String uuid = (String) claims.get("login_user_key");
                String userKey = getTokenKey(uuid);
                return (LoginUser) redisTemplate.opsForValue().get(userKey);
            } catch (Exception e) {
                // Token 解析失败或过期
                throw e;
            }
        }
        return null;
    }

    /**
     * 获取登录用户 (从 Redis)
     */
    public LoginUser getLoginUser(String token) {
        // 获取请求携带的令牌
        if (StringUtils.hasText(token)) {
            try {
                Claims claims = parseToken(token);
                // 解析对应的 UUID
                String uuid = (String) claims.get("login_user_key");
                String userKey = getTokenKey(uuid);
                return (LoginUser) redisTemplate.opsForValue().get(userKey);
            } catch (Exception e) {
                // Token 解析失败或过期
                throw e;
            }
        }
        return null;
    }

    /**
     * 删除用户身份信息 (用于注销)
     * @param token 完整的 JWT 字符串 (Bearer xxx)
     */
    public void deleteToken(String token) {
        if (StringUtils.hasText(token)) {
            // 1. 去掉 Bearer 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            try {
                // 2. 解析 JWT 获取 Payload 中的 UUID
                Claims claims = parseToken(token);
                String uuid = (String) claims.get("login_user_key");
                String username = claims.getSubject();
                String deviceId = (String) claims.get("device_id");
                // 3. 拼接 Redis Key 并删除
                String userKey = getTokenKey(uuid);
                redisTemplate.delete(userKey);
                //4.删除lastLoginToken
                String lastLoginKey = getUserLastTokenKey(username,deviceId);
                redisTemplate.delete(lastLoginKey);
                //5.删除refreshToken
                String refreshToken = getRefreshJtiKey(uuid);
                redisTemplate.delete(refreshToken);
            } catch (Exception e) {
                // Token 可能已过期或非法，解析失败则无需处理
            }
        }
    }

    // --- 辅助方法 ---

    public String createJwt(Map<String, Object> claims, String subject,long ttlMillis) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    private String getHeaderToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public String getTokenKey(String uuid) {
        return "login_tokens:" + uuid;
    }

    public String getUserLastTokenKey(String username,String deviceId) {
        return USER_LAST_TOKEN_KEY + username+":"+deviceId;
    }

    public String getRefreshJtiKey(String uuid) {
        return "refresh_jti:" + uuid;
    }

}