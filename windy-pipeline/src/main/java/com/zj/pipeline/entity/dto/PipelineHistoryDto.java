package com.zj.pipeline.entity.dto;

import com.zj.pipeline.entity.po.PipelineHistory;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.NotEmpty;

/**
 * @author falcon
 * @since 2021/10/15
 */
@Builder
@Data
public class PipelineHistoryDto {

  /**
   * 流水线Id
   * */
  @NotEmpty
  private String pipelineId;

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
  private String pipelineResult;

  /**
   * 创建时间
   * */
  private Long createTime;

  public static PipelineHistoryDto toPipelineHistoryDto(PipelineHistory pipelineHistory){
    PipelineHistoryDto pipelineHistoryDto = PipelineHistoryDto.builder().build();
    BeanUtils.copyProperties(pipelineHistory,pipelineHistoryDto);
    return pipelineHistoryDto;
  }

  public static PipelineHistory toPipelineHistory(PipelineHistoryDto pipelineHistoryDto){
    PipelineHistory history = new PipelineHistory();
    BeanUtils.copyProperties(pipelineHistoryDto, history);
    return history;
  }
}
