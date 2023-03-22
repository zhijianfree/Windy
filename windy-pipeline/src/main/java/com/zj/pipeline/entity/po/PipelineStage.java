package com.zj.pipeline.entity.po;

import lombok.Data;

/**
 * @author falcon
 * @since 2022/6/13
 */
@Data
public class PipelineStage {

  private Long id;

  private String stageId;

  private String stageName;

  private Integer type;

  private String pipelineId;

  private Long createTime;

  private Long updateTime;

}
