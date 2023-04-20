package com.zj.pipeline.executer.vo;

import com.zj.pipeline.executer.enums.ProcessStatus;
import lombok.Builder;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/3/30
 */
@Data
@Builder
public class PipelineStatusEvent {

  private String nodeId;

  private String recordId;

  private ProcessStatus processStatus;
}
