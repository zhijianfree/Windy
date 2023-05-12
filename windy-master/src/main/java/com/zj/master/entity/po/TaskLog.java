package com.zj.master.entity.po;

import com.zj.master.entity.enums.LogType;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/5/12
 */
@Data
public class TaskLog {

  private Long id;

  /**
   * 日志Id
   * */
  private String logId;

  /**
   * 日志类型{@link LogType}
   * */
  private Integer logType;

  /**
   * 任务来源Id
   * */
  private String sourceId;

  /**
   * 任务来源名称
   * */
  private String sourceName;

  /**
   * 执行任务节点IP
   * */
  private String nodeIp;

  /**
   * 日志状态
   * */
  private Integer logStatus;

  private Long createTime;

  private Long updateTime;
}
