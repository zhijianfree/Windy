package com.zj.client.handler.pipeline.executer.vo;

import com.zj.common.enums.ProcessStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 流水线状态扭转事件
 *
 * @author guyuelan
 * @since 2023/3/30
 */
@Data
@Builder
public class PipelineStatusEvent {

  /**
   * 处理状态
   */
  private ProcessStatus processStatus;

  /**
   * 异常消息
   */
  private List<String> errorMsg;

  /**
   * 节点执行信息详情
   */
  private TaskNode taskNode;

  private Map<String, Object> context;
}
