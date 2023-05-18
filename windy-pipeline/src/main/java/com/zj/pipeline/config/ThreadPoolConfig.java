package com.zj.pipeline.config;

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
}
