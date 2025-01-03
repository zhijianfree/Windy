package com.zj.common.entity.dto;

import com.zj.common.adapter.monitor.collector.PhysicsCollect;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Data
public class ClientCollectDto {

  /**
   * Client当前版本
   */
  private String version;

  /**
   * 采集的物理监控信息
   */
  private PhysicsCollect physics;

  /**
   * client 等待处理任务队列数
   */
  private Integer waitQuerySize;
}
