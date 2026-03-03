package com.mogo.project.modules.system.manager;

import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DingTalkManager {

    private final StringRedisTemplate redisTemplate;

    @Value("${dingtalk.app-key}")
    private String appKey;

    @Value("${dingtalk.app-secret}")
    private String appSecret;

    // Redis Key 前缀
    private static final String DING_TOKEN_KEY = "dingtalk:access_token";

    // 钉钉 Token 官方有效期通常是 7200秒，我们设为 7000秒 (留200秒缓冲期)
    private static final long TOKEN_EXPIRE_SECONDS = 7000;

    /**
     * 获取企业内部应用的 AccessToken (带缓存)
     */
    public String getAccessToken() {
        // 1. 先查 Redis
        String cachedToken = redisTemplate.opsForValue().get(DING_TOKEN_KEY);
        if (StringUtils.hasText(cachedToken)) {
            return cachedToken;
        }

        // 2. 加锁防止高并发击穿 (可选，如果是单体应用 synchronized 够用，集群建议用分布式锁)
        // 这里简单用 synchronized，因为获取 Token 频率极低，性能损耗忽略不计
        synchronized (this) {
            // 双重检查
            cachedToken = redisTemplate.opsForValue().get(DING_TOKEN_KEY);
            if (StringUtils.hasText(cachedToken)) {
                return cachedToken;
            }

            // 3. 真正调用钉钉接口
            try {
                com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
                config.protocol = "https";
                config.regionId = "central";
                Client client = new Client(config);

                GetAccessTokenRequest request = new GetAccessTokenRequest()
                        .setAppKey(appKey)
                        .setAppSecret(appSecret);

                GetAccessTokenResponse response = client.getAccessToken(request);
                String newToken = response.getBody().getAccessToken();

                // 官方返回的有效期字段，单位秒 (通常是 7200)
                Long expireIn = response.getBody().getExpireIn();

                // 4. 存入 Redis，过期时间设置得比官方时间略短 (如减少 200秒)
                // 这样能确保 Redis 里有值时，Token 一定是有效的
                long safeExpire = (expireIn != null && expireIn > 200) ? (expireIn - 200) : TOKEN_EXPIRE_SECONDS;

                redisTemplate.opsForValue().set(DING_TOKEN_KEY, newToken, safeExpire, TimeUnit.SECONDS);

                log.info("钉钉 AccessToken 已刷新，有效期: {}秒", safeExpire);
                return newToken;

            } catch (Exception e) {
                log.error("获取钉钉AccessToken失败", e);
                throw new RuntimeException("钉钉服务异常");
            }
        }
    }
}