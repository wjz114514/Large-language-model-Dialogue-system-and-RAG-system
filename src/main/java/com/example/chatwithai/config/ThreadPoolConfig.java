package com.example.chatwithai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3); // 核心线程数设为3
        executor.setMaxPoolSize(3); // 最大线程数设为3
        executor.setQueueCapacity(10); // 队列容量设为10
        executor.initialize();
        return executor;
    }
}

