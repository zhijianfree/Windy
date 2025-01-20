package com.zj.pipeline.config;

import com.zj.common.adapter.pool.WindyThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author guyuelan
 * @since 2023/4/14
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

  @Bean("webHookExecutorPool")
  public Executor getWebHookExecutor() {
    WindyThreadPool windyThreadPool = WindyThreadPool.builder()
            .corePoolSize(5)
            .queueSize(100)
            .maxPoolSize(20)
            .timeout(600L)
            .allowCoreThreadTimeOut(false)
            .build();
    windyThreadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
    windyThreadPool.setThreadNamePrefix("web-hook-thread-");
    return windyThreadPool;
  }
}
