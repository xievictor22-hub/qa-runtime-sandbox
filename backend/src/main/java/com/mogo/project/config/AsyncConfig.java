package com.mogo.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * 用于处理系统中的异步任务（如日志记录、文件上传后处理等）
 */
@Configuration
@EnableAsync // 开启异步注解支持
public class AsyncConfig {

    @Bean("mogoTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 核心线程数：根据 CPU 核心数配置，通常为 CPU 核数 + 1
        // 假设服务器为 4 核，这里配置为 5。实际可读取配置文件。
        executor.setCorePoolSize(5);

        // 2. 最大线程数：当队列满了之后，允许创建的最大线程数
        executor.setMaxPoolSize(20);

        // 3. 队列容量：缓冲执行任务的队列
        executor.setQueueCapacity(200);

        // 4. 线程活跃时间 (秒)
        executor.setKeepAliveSeconds(60);

        // 5. 线程名称前缀，方便日志排查
        executor.setThreadNamePrefix("mogo-async-");

        // 6. 拒绝策略：当线程池和队列都满了，如何处理新任务
        // CallerRunsPolicy: 由调用者所在的线程来执行该任务（通过降低吞吐量来保证任务不丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        // ★★★ 核心修改：使用 Security 装饰器包装线程池 ★★★
        // 这样子线程就能获取到 LoginUser 了
        return new DelegatingSecurityContextExecutor(executor);
    }
}