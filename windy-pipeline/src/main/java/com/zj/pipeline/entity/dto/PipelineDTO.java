package com.zj.pipeline.entity.dto;

import com.alibaba.fastjson.JSON;
import com.zj.pipeline.entity.po.Pipeline;
import java.util.List;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Data
public class PipelineDTO {

  /**
   * 流水线Id
   */
  private String pipelineId;

  /**
   * 流水线名称
   */
  @NotEmpty
  private String pipelineName;

  /**
   * 服务Id
   */
  @NotEmpty
  private String serviceId;

  /**
   * 服务名
   */
  private String ServiceName;

  /**
   * 流水线创建者
   */
  @NotEmpty
  private String creator;

  /**
   * 流水线类型
   */
  @NotNull
  @Min(1)
  @Max(3)
  private Integer pipelineType;

  /**
   * 执行方式
   * */
  @NotNull
  @Min(1)
  @Max(3)
  private Integer executeType;

  /**
   * 流水线配置
   */
  private String pipelineConfig;

  /**
   * 流水线状态
   */
  private Integer pipelineStatus;

  @NotEmpty
  private List<PipelineStageDTO> stageList;

  public PipelineDTO() {
  }

  public static PipelineDTO toPipelineDto(Pipeline pipeline) {
    if (Objects.isNull(pipeline)) {
      return null;
    }

    return JSON.parseObject(JSON.toJSONString(pipeline), PipelineDTO.class);

  }

  public static Pipeline toPipeline(PipelineDTO pipelineDTO) {
    Pipeline pipeline = new Pipeline();
    pipeline.setPipelineId(pipelineDTO.getPipelineId());
    pipeline.setPipelineConfig(pipelineDTO.getPipelineConfig());
    pipeline.setPipelineType(pipelineDTO.getPipelineType());
    pipeline.setExecuteType(pipelineDTO.getExecuteType());
    pipeline.setCreator(pipelineDTO.getCreator());
    pipeline.setPipelineName(pipelineDTO.getPipelineName());
    pipeline.setServiceId(pipelineDTO.getServiceId());
    return pipeline;
  }
}
