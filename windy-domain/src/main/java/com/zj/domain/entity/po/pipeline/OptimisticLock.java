package com.zj.domain.entity.po.pipeline;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/7/19
 */
@Data
public class OptimisticLock {

  private Long id;

  /**
   * 业务类型
   * */
  private String bizCode;

  /**
   * 节点名称
   * */
  private String nodeName;

  /**
   * 当前节点IP
   * */
  private String ip;

  /**
   * 开始时间
   * */
  private Long startTime;

  /**
   * 锁持有结束时间
   * */
  private Long endTime;

  /**
   * 锁版本号
   * */
  private Long version;

}
