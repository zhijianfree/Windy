package com.zj.common.monitor.collector;

import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Data
public class PhysicsCollect {

  private String ip;

  private String cpu;

  private String heap;

  private Integer threads;

  private List<GarbageHistory> histories;

  @Data
  public static class GarbageHistory{

    private String collector;

    private Long collectCount;

    private String collectTime;
  }
}
