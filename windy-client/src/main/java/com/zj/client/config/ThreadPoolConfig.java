package com.zj.client.config;

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

  @Bean("pipelinePool")
  public Executor getPipelineExecutor() {
    WindyThreadPool windyThreadPool = WindyThreadPool.builder()
            .corePoolSize(10)
            .maxPoolSize(30)
            .allowCoreThreadTimeOut(false)
            .queueSize(100).build();
    windyThreadPool.setThreadNamePrefix("pipeline-thread-");
    return windyThreadPool;
  }

  @Bean("loopQueryPool")
  public Executor getQueryLooperExecutor() {
    WindyThreadPool windyThreadPool = WindyThreadPool.builder()
            .corePoolSize(20)
            .maxPoolSize(80)
            .allowCoreThreadTimeOut(false)
            .queueSize(1000).build();
    windyThreadPool.setThreadNamePrefix("query-loop-thread-");
    return windyThreadPool;
  }

  @Bean("gitOperatePool")
  public Executor gitOperateExecutor() {
    WindyThreadPool windyThreadPool = WindyThreadPool.builder().timeout(60 * 60L)
            .corePoolSize(10)
            .maxPoolSize(30)
            .allowCoreThreadTimeOut(false)
            .queueSize(200)
            .build();
    windyThreadPool.setThreadNamePrefix("buildCode-thread-");
    return windyThreadPool;
  }

  @Bean("eventBusPool")
  public Executor getEventBusPool() {
    WindyThreadPool windyThreadPool = WindyThreadPool.builder().timeout(60L)
            .corePoolSize(10)
            .maxPoolSize(20)
            .allowCoreThreadTimeOut(false)
            .queueSize(100)
            .build();
    windyThreadPool.setThreadNamePrefix("event-bus-");
    return windyThreadPool;
  }

  @Bean("generatePool")
  public Executor getGeneratePool() {
    WindyThreadPool windyThreadPool = WindyThreadPool.builder().timeout(10 * 60L)
            .corePoolSize(10)
            .maxPoolSize(20)
            .allowCoreThreadTimeOut(false)
            .queueSize(100)
            .build();
    windyThreadPool.setThreadNamePrefix("generate-");
    return windyThreadPool;
  }

  @Bean("featureExecutePool")
  public Executor getFeatureExecutePool() {
    WindyThreadPool windyThreadPool = WindyThreadPool.builder().timeout(5 * 60L)
            .corePoolSize(10)
            .maxPoolSize(40)
            .allowCoreThreadTimeOut(false)
            .queueSize(100)
            .build();
    windyThreadPool.setThreadNamePrefix("feature-");
    return windyThreadPool;
  }

  @Bean("cleanDirtyDataExecutePool")
  public Executor getCleanDirtyDataExecutePool() {
    WindyThreadPool windyThreadPool = WindyThreadPool.builder().timeout(60L)
            .corePoolSize(10)
            .maxPoolSize(40)
            .allowCoreThreadTimeOut(false)
            .queueSize(100)
            .build();
    windyThreadPool.setThreadNamePrefix("clean-dirty-");
    //清理脏数据处理任务，堆积就直接清除
    windyThreadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
    return windyThreadPool;
  }

  @Bean("asyncExecuteFeaturePool")
  public Executor getAsyncExecuteFeaturePool() {
    WindyThreadPool windyThreadPool = WindyThreadPool.builder().timeout(5 * 60L)
            .corePoolSize(10)
            .maxPoolSize(40)
            .allowCoreThreadTimeOut(false)
            .queueSize(1000)
            .build();
    windyThreadPool.setThreadNamePrefix("async-exe-");
    return windyThreadPool;
  }
}
