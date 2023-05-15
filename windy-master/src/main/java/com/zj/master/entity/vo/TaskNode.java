package com.zj.master.entity.vo;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/5/23
 */
@Data
public class TaskNode {
  /**
   * 流水线节点Id
   * */
  private String nodeId;

  /**
   * 流水线节点名称
   * */
  private String name;

  /**
   * 运行记录Id
   * */
  private String recordId;

  /**
   * 流水线历史记录Id
   * */
  private String historyId;

  /**
   * 执行类型，默认Http
   * */
  private String executeType;

  /**
   * 出发节点执行的第三方接口信息
   * */
  private RequestContext requestContext;

  /**
   * 刷新节点任务状态的第三方接口信息
   * */
  private RefreshContext refreshContext;

  /**
   * 节点运行配置信息
   * */
  private NodeConfig nodeConfig;

  /**
   * 任务开始执行的时间点
   * */
  private Long executeTime;
}
