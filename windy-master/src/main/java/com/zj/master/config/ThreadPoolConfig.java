package com.zj.master.config;

import com.zj.common.adapter.pool.WindyThreadPool;
import java.util.concurrent.Executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author guyuelan
 * @since 2023/4/14
 */
@Slf4j
@Configuration
public class ThreadPoolConfig {

  @Bean("pipelineExecutorPool")
  public Executor getPipelineExecutor() {
    WindyThreadPool windyThreadPool = WindyThreadPool.builder()
            .maxPoolSize(40)
            .corePoolSize(20)
            .timeout(3600 * 3L)
            .queueSize(100)
            .allowCoreThreadTimeOut(false).build();
    windyThreadPool.setThreadNamePrefix("master-pipeline-");
    return windyThreadPool;
  }

  @Bean("featureExecutorPool")
  public Executor getFeatureExecutor() {
    WindyThreadPool windyThreadPool = WindyThreadPool.builder()
            .maxPoolSize(40)
            .corePoolSize(20)
            .timeout(3600 * 3L)
            .queueSize(100)
            .allowCoreThreadTimeOut(false).build();
    windyThreadPool.setThreadNamePrefix("master-feature-");
    return windyThreadPool;
  }
}
