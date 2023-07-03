package com.zj.master.config;

import com.zj.common.monitor.pool.WindyThreadPool;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
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
    WindyThreadPool windyThreadPool = new WindyThreadPool();
    windyThreadPool.setCorePoolSize(20);
    windyThreadPool.setMaxPoolSize(40);
    windyThreadPool.setTimeout(3600 * 3L);
    windyThreadPool.setAllowCoreThreadTimeOut(false);
    windyThreadPool.setQueueSize(100);
    windyThreadPool.setThreadNamePrefix("master-pipeline-");
    return windyThreadPool;
  }

  @Bean("featureExecutorPool")
  public Executor getFeatureExecutor() {
    WindyThreadPool windyThreadPool = new WindyThreadPool();
    windyThreadPool.setCorePoolSize(20);
    windyThreadPool.setMaxPoolSize(40);
    windyThreadPool.setTimeout(3600 * 3L);
    windyThreadPool.setAllowCoreThreadTimeOut(false);
    windyThreadPool.setQueueSize(100);
    windyThreadPool.setThreadNamePrefix("master-feature-");
    return windyThreadPool;
  }
}
