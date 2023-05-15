package com.zj.domain.entity.po.feature;

import lombok.Data;

@Data
public class TaskRecord {

  private Long id;

  /**
   * 任务记录ID
   * */
  private String recordId;

  /**
   * 任务ID
   * */
  private String taskId;

  /**
   * 创建者
   * */
  private String userId;

  /**
   * 执行状态
   * */
  private Integer status;

  /**
   * 任务名称
   * */
  private String taskName;

  /**
   * 测试集Id
   * */
  private String testCaseId;

  /**
   * 任务配置
   * */
  private String taskConfig;

  /**
   * 执行机器
   * */
  private String machines;

  private Long createTime;

  private Long updateTime;

}
