package com.zj.client.config;

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
    return new ThreadPoolExecutor(5, 10, 3, TimeUnit.HOURS, new LinkedBlockingQueue<>(100),
        new CallerRunsPolicy());
  }

  @Bean("queryLooperExecutorPool")
  public ExecutorService getQueryLooperExecutor() {
    return new ThreadPoolExecutor(20, 60, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(1000),
        new CallerRunsPolicy());
  }

  @Bean("webHookExecutorPool")
  public ExecutorService getWebHookExecutor() {
    return new ThreadPoolExecutor(5, 20, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(100),
        (r, executor) -> {
          if (!executor.isShutdown()) {
            executor.getQueue().poll();
            executor.execute(r);
          }
          log.info("discard old");
        });
  }

  @Bean("gitOperateExecutor")
  public ExecutorService gitOperateExecutor() {
    return new ThreadPoolExecutor(10, 30, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>(50),
        (r, executor) -> {
          if (!executor.isShutdown()) {
            executor.getQueue().poll();
            executor.execute(r);
          }
          log.info("discard old");
        });
  }
}
