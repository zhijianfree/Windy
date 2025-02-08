package com.zj.domain.entity.bo.pipeline;

import com.zj.common.entity.pipeline.PipelineConfig;
import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Data
public class PipelineBO {

  private Long id;

  /**
   * 流水线Id
   */
  private String pipelineId;

  /**
   * 流水线名称
   */
  private String pipelineName;

  /**
   * 服务Id
   */
  private String serviceId;

  /**
   * 服务名
   */
  private String ServiceName;

  /**
   * 流水线类型
   */
  private Integer pipelineType;

  /**
   * 执行方式
   * */
  private Integer executeType;

  /**
   * 流水线配置
   */
  private PipelineConfig pipelineConfig;

  /**
   * 流水线状态
   */
  private Integer pipelineStatus;

  private List<PipelineStageBO> stageList;
}
