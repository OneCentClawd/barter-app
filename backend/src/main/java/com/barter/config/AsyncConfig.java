package com.barter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // 使用 Spring 默认的异步执行器
    // 如需自定义线程池，可以在这里配置 @Bean TaskExecutor
}
