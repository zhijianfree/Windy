package com.zj.common.adapter.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/24
 */
@Slf4j
@Component
public class InstanceMonitor{

  private final Long serviceStartTime = System.currentTimeMillis();
  private boolean isUnStable = true;

  /**
   * 服务启动后，需要一定的时间达到稳定性，所以这里配置一个delay时间
   */
  public boolean isUnStable() {
    if (isUnStable){
      isUnStable = (System.currentTimeMillis() - serviceStartTime) < 30000;
    }
    return isUnStable;
  }
}
