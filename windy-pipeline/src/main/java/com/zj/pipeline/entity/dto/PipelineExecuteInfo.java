package com.zj.pipeline.entity.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/4/6
 */
@Data
@Builder
public class PipelineExecuteInfo {

  private List<NodeStatus> nodeStatusList;

  private Integer pipelineStatus;
}
