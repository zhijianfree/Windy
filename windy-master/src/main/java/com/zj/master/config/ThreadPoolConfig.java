package com.zj.master.config;

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
  public ExecutorService getPipelineExecutor() {
    return new ThreadPoolExecutor(20, 40, 3, TimeUnit.HOURS, new LinkedBlockingQueue<>(100),
        new CallerRunsPolicy());
  }

  @Bean("featureExecutorPool")
  public ExecutorService getFeatureExecutor() {
    return new ThreadPoolExecutor(20, 40, 3, TimeUnit.HOURS, new LinkedBlockingQueue<>(100),
        new CallerRunsPolicy());
  }
}
