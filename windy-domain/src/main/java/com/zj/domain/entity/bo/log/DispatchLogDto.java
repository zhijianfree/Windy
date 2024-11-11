package com.zj.domain.entity.bo.log;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/12
 */
@Data
public class DispatchLogDto {

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
   * 来源记录Id
   * */
  private String sourceRecordId;

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
   * 乐观锁版本号
   * */
  private Integer lockVersion;

  private Long createTime;

  private Long updateTime;
}
