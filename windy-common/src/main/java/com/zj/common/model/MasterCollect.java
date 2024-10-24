package com.zj.common.model;

import com.zj.common.monitor.collector.PhysicsCollect;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Data
public class MasterCollect {

  private PhysicsCollect physics;

  private Integer taskCount;
}
