package com.zj.domain.entity.dto.pipeline;

import lombok.Data;
import javax.validation.constraints.NotEmpty;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Data
public class PipelineHistoryDto {

  /**
   * 流水线Id
   * */
  @NotEmpty
  private String pipelineId;

  private String historyId;

  /**
   * 流水线名称
   * */
  @NotEmpty
  private String pipelineName;

  /**
   * 流水线执行的分支
   * */
  @NotEmpty
  private String branch;

  /**
   * 流水线执行人
   * */
  @NotEmpty
  private String executor;

  /**
   * 流水线配置
   * */
  private String pipelineConfig;

  /**
   * 流水线结果
   * */
  @NotEmpty
  private Integer pipelineStatus;

  /**
   * 创建时间
   * */
  private Long createTime;

  private Long updateTime;
}
