package com.zj.domain.mapper.log;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/12
 */
@Data
public class SubTaskLog {

  private Long id;

  /**
   * 子任务Id
   * */
  private String subTaskId;

  /**
   * 子任务名称
   * */
  private String subTaskName;

  /**
   * 执行Id
   * */
  private String executeId;

  /**
   * 执行任务参数
   * */
  private String executeParam;

  /**
   * 父任务Id
   * */
  private String logId;

  private Long createTime;

  private Long updateTime;
}
