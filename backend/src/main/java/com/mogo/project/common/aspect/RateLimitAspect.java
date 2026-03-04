package com.mogo.project.common.aspect;

import com.mogo.project.common.annotation.RateLimit;
import com.mogo.project.common.exception.ServiceException;
import com.mogo.project.common.util.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {
    private final RedisTemplate<String, Object> redisTemplate;

    @Before("@annotation(rateLimit)")
    public void before(JoinPoint jp, RateLimit rateLimit) {
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String key = "rate_limit:" + req.getRequestURI() + ":" + IpUtils.getIpAddr(req);
        Long current = redisTemplate.opsForValue().increment(key);
        if (current != null && current == 1L) {
            redisTemplate.expire(key, rateLimit.seconds(), TimeUnit.SECONDS);
        }
        if (current != null && current > rateLimit.count()) {
            throw new ServiceException(rateLimit.message(),429);
        }
    }
}
