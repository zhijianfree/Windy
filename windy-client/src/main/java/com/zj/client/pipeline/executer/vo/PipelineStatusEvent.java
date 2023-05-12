package com.zj.client.pipeline.executer.vo;

import com.zj.common.enums.ProcessStatus;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/30
 */
@Data
@Builder
public class PipelineStatusEvent {

  private ProcessStatus processStatus;

  private List<String> errorMsg;

  private TaskNode taskNode;
}
