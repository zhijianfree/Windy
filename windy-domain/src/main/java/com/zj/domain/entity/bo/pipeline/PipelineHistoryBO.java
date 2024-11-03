package com.zj.domain.entity.bo.pipeline;

import com.zj.common.entity.pipeline.PipelineConfig;
import lombok.Data;
import javax.validation.constraints.NotEmpty;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Data
public class PipelineHistoryBO {

  /**
   * 流水线Id
   * */
  @NotEmpty
  private String pipelineId;

  /**
   * 历史ID
   */
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
   * 流水线配置
   * */
  private PipelineConfig pipelineConfig;

  /**
   * 流水线结果
   * */
  @NotEmpty
  private Integer pipelineStatus;

  /**
   * 创建时间
   * */
  private Long createTime;

  /**
   * 修改时间
   */
  private Long updateTime;
}
