package com.mogo.project.modules.system.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.common.util.IpUtils;
import com.mogo.project.modules.auth.model.LoginUser;
import com.mogo.project.modules.system.mapper.SysClientErrorLogMapper;
import com.mogo.project.modules.system.model.dto.ClientErrorReportDto;
import com.mogo.project.modules.system.model.entity.SysClientErrorLog;
import com.mogo.project.modules.system.service.ISysClientErrorLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SysClientErrorLogServiceImpl extends ServiceImpl<SysClientErrorLogMapper, SysClientErrorLog> implements ISysClientErrorLogService {

    @Resource
    private SysClientErrorLogMapper mapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${client-error.security.max-per-minute:120}")
    private int maxPerMinute;

    @Value("${client-error.security.allow-anonymous:true}")
    private boolean allowAnonymous;

    @Value("${client-error.security.sign-window-seconds:300}")
    private long signWindowSeconds;

    @Value("${client-error.alert.threshold-5m:30}")
    private long alertThreshold5m;

    @Value("${client-error.archive.retention-days:90}")
    private int retentionDays;

    /**
     * appKey -> appSecret，示例：web-portal:xxxx,mobile-app:yyyy
     */
    @Value("${client-error.security.app-secrets:}")
    private String appSecrets;

    @Override
    public void validateAndGuard(ClientErrorReportDto dto, HttpServletRequest request) {
        String ip = IpUtils.getIpAddr(request);

        // 1) 限流：IP + appName + minute
        long minuteBucket = System.currentTimeMillis() / 60000;
        String rateKey = String.format("client_error:rate:%s:%s:%d", dto.getAppName(), ip, minuteBucket);
        Long current = redisTemplate.opsForValue().increment(rateKey);
        if (current != null && current == 1L) {
            redisTemplate.expire(rateKey, 2, TimeUnit.MINUTES);
        }
        if (current != null && current > maxPerMinute) {
            throw new ServiceException( "上报过于频繁，请稍后重试",429);
        }

        // 2) 签名鉴权（匿名模式下也必须签名）
        if (allowAnonymous) {
            verifySignature(dto);
        }
    }
    @Override
    public SysClientErrorLog buildEntity(ClientErrorReportDto dto, HttpServletRequest req, LoginUser loginUser) {
        SysClientErrorLog entity = new SysClientErrorLog();
        entity.setAppName(safeTrim(dto.getAppName(), 64));
        entity.setEnv(safeTrim(dto.getEnv(), 32));
        entity.setLevel(safeTrim(dto.getLevel(), 16));
        entity.setErrorType(safeTrim(dto.getErrorType(), 64));
        entity.setMessage(maskSensitive(safeTrim(dto.getMessage(), 1000)));
        entity.setStack(maskSensitive(safeTrim(dto.getStack(), 20000)));
        entity.setPageUrl(safeTrim(dto.getPageUrl(), 1000));
        entity.setRoutePath(safeTrim(dto.getRoutePath(), 255));
        entity.setDeviceId(safeTrim(dto.getDeviceId(), 128));
        entity.setTraceId(safeTrim(dto.getTraceId(), 64));
        entity.setExtraJson(maskSensitive(safeTrim(dto.getExtra(), 20000)));

        entity.setIp(IpUtils.getIpAddr(req));
        entity.setUserAgent(safeTrim(req.getHeader("User-Agent"), 512));

        if (loginUser != null && loginUser.getSysUser() != null) {
            entity.setUserId(loginUser.getSysUser().getId());
            entity.setUsername(loginUser.getUsername());
        }

        LocalDateTime occurAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(dto.getOccurTime()), ZoneId.systemDefault());
        entity.setOccurTime(occurAt);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    @Override
    @Async("mogoTaskExecutor")
    public void recordAsync(SysClientErrorLog entity) {
        try {
            mapper.insert(entity);
            evaluateAndAlert(entity);
        } catch (Exception e) {
            log.error("前端异常日志入库失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 分级告警：fatal 立即告警；同类错误 5 分钟超阈值告警
     */
    protected void evaluateAndAlert(SysClientErrorLog entity) {
        boolean fatal = "fatal".equalsIgnoreCase(entity.getLevel());
        String fingerprint = fingerprint(entity);
        String countKey = "client_error:alert:5m:" + fingerprint;
        Long count = redisTemplate.opsForValue().increment(countKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(countKey, 5, TimeUnit.MINUTES);
        }

        if (fatal || (count != null && count >= alertThreshold5m)) {
            String dedupKey = "client_error:alert:dedup:" + fingerprint;
            Boolean first = redisTemplate.opsForValue().setIfAbsent(dedupKey, 1, 5, TimeUnit.MINUTES);
            if (Boolean.TRUE.equals(first)) {
                // 当前先记录告警日志；后续可对接钉钉/邮件/告警平台
                log.warn("[前端异常告警] app={}, env={}, level={}, type={}, count5m={}, msg={}",
                        entity.getAppName(), entity.getEnv(), entity.getLevel(), entity.getErrorType(), count, entity.getMessage());
            }
        }
    }

    /**
     * 归档策略（简化版）：按 retentionDays 清理热表历史数据
     */
    @Override
    @Scheduled(cron = "0 20 3 * * ?")
    public void archiveAndCleanup() {
        LocalDateTime deadline = LocalDateTime.now().minusDays(retentionDays);
        int deleted = mapper.delete(new LambdaQueryWrapper<SysClientErrorLog>()
                .lt(SysClientErrorLog::getCreatedAt, deadline));
        if (deleted > 0) {
            log.info("[前端异常归档清理] retentionDays={}, deleted={}", retentionDays, deleted);
        }
    }

    private void verifySignature(ClientErrorReportDto dto) {
        if (!StringUtils.hasText(dto.getAppKey())) {
            throw new ServiceException( "appKey不能为空",401);
        }
        if (dto.getTimestamp() == null) {
            throw new ServiceException( "timestamp不能为空",401);
        }
        if (!StringUtils.hasText(dto.getSign())) {
            throw new ServiceException( "sign不能为空",401);
        }

        long now = System.currentTimeMillis() / 1000;
        if (Math.abs(now - dto.getTimestamp()) > signWindowSeconds) {
            throw new ServiceException(401, "签名时间已过期");
        }

        String secret = getSecretByAppKey(dto.getAppKey());
        if (!StringUtils.hasText(secret)) {
            throw new ServiceException(401, "非法appKey");
        }

        // 待签名串：appKey|timestamp|appName|env|level|errorType|message
        String payload = String.join("|",
                nullToEmpty(dto.getAppKey()),
                String.valueOf(dto.getTimestamp()),
                nullToEmpty(dto.getAppName()),
                nullToEmpty(dto.getEnv()),
                nullToEmpty(dto.getLevel()),
                nullToEmpty(dto.getErrorType()),
                nullToEmpty(dto.getMessage())
        );

        String expected = hmacSha256Hex(payload, secret);
        if (!expected.equalsIgnoreCase(dto.getSign())) {
            throw new ServiceException(401, "签名校验失败");
        }
    }

    private String getSecretByAppKey(String appKey) {
        if (!StringUtils.hasText(appSecrets)) {
            return null;
        }
        String[] pairs = appSecrets.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2 && appKey.equals(kv[0].trim())) {
                return kv[1].trim();
            }
        }
        return null;
    }

    private String hmacSha256Hex(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format(Locale.ROOT, "%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new ServiceException(500, "签名计算失败");
        }
    }

    private String fingerprint(SysClientErrorLog e) {
        String raw = String.join("|",
                nullToEmpty(e.getAppName()),
                nullToEmpty(e.getEnv()),
                nullToEmpty(e.getErrorType()),
                nullToEmpty(e.getMessage())
        );
        return DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String maskSensitive(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        String masked = text;
        String[] patterns = {
                "(?i)(authorization\\s*[:=]\\s*)([^\\s,;]+)",
                "(?i)(token\\s*[:=]\\s*)([^\\s,;]+)",
                "(?i)(refreshToken\\s*[:=]\\s*)([^\\s,;]+)",
                "(?i)(password\\s*[:=]\\s*)([^\\s,;]+)"
        };
        for (String p : patterns) {
            masked = masked.replaceAll(p, "$1***");
        }
        return masked;
    }

    private String safeTrim(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
