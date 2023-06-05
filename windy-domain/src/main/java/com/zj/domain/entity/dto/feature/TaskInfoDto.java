package com.zj.domain.entity.dto.feature;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/12/29
 */
@Data
public class TaskInfoDto {

  private Long id;

  /**
   * 任务ID
   * */
  private String taskId;

  /**
   * 任务名称
   * */
  private String taskName;

  /**
   * 创建者
   * */
  private String userId;

  /**
   * 服务ID
   * */
  private String serviceId;

  /**
   * 测试集Id
   * */
  private String testCaseId;

  /**
   * 任务描述
   * */
  private String description;

  /**
   * 任务配置
   * */
  private String taskConfig;

  /**
   * 当前任务是否在执行
   * */
  private Boolean isRunning;

  /**
   * 执行机器
   * */
  private String machines;

  private Long createTime;

  private Long updateTime;
}
