package com.zj.common.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/5/24
 */
@Slf4j
@Component
public class InstanceMonitor implements CommandLineRunner {

  private Long serviceStartTime;

  private boolean isSuitable;

  @Override
  public void run(String... args) throws Exception {
    serviceStartTime = System.currentTimeMillis();
  }

  /**
   * 服务启动后，需要一定的时间达到稳定性，所以这里配置一个delay时间
   */
  public boolean isSuitable() {
    if (!isSuitable){
      isSuitable = (System.currentTimeMillis() - serviceStartTime) > 30000;
    }
    return isSuitable;

  }
}
