package com.zj.domain.entity.po.log;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/12
 */
@Data
public class DispatchLog {

  private Long id;

  /**
   * 日志Id
   * */
  private String logId;

  /**
   * 日志类型
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
   * 执行任务master节点IP
   * */
  private String nodeIp;

  /**
   * 日志状态
   * */
  private Integer logStatus;

  /**
   * 来源记录Id
   * */
  private String sourceRecordId;

  /**
   * 乐观锁版本号
   * */
  private Integer lockVersion;

  private Long createTime;

  private Long updateTime;
}
