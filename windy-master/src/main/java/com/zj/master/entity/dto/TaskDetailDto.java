package com.zj.master.entity.dto;

import com.zj.common.enums.LogType;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/12
 */
@Data
public class TaskDetailDto {

  /**
   * 执行任务来源Id(测试集合Id，流水线Id，用例任务Id)
   * 如果是测试集那么sourceId就是用例Id列表
   * */
  private String sourceId;

  /**
   * 执行任务来源名称
   * */
  private String sourceName;

  /**
   * 任务类型{@link LogType}
   * */
  private Integer type;
}
