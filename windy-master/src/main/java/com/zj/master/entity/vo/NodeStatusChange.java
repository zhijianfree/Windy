package com.zj.master.entity.vo;

import com.zj.common.enums.ProcessStatus;
import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/29
 */
@Data
@Builder
public class NodeStatusChange {

  /**
   * 流水线历史记录Id
   * */
  private String historyId;

  /**
   * 子节点Id
   * */
  private String nodeId;

  /**
   * 流水线Id
   * */
  private String pipelineId;

  /**
   * 节点运行结果状态
   * */
  private ProcessStatus processStatus;

  /**
   * 分发client任务的记录Id {@link com.zj.domain.entity.po.log.DispatchLog}
   * */
  private String logId;
}
