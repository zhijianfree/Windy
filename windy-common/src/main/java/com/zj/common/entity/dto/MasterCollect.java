package com.zj.common.entity.dto;

import com.zj.common.adapter.monitor.collector.PhysicsCollect;
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