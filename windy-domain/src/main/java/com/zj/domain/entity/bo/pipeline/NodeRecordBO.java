package com.zj.domain.entity.bo.pipeline;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NodeRecordBO {

  private Long id;

  /**
   * 流水线节点Id
   * */
  private String nodeId;

  /**
   * 节点运行记录Id
   * */
  private String recordId;

  /**
   * 所属流水线记录Id
   * */
  private String historyId;

  /**
   * 记录状态
   * */
  private Integer code;

  /**
   * 执行的结果
   * */
  private List<String> result;

  /**
   * 任务执行上下文,作用域整个流水线
   * */
  private Map<String, Object> pipelineContext;

  /**
   * 记录状态
   * */
  private Integer status;

}
