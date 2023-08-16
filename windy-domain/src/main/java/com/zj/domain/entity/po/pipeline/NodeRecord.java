package com.zj.domain.entity.po.pipeline;

import lombok.Data;

@Data
public class NodeRecord {

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
  private String result;

  /**
   * 任务执行上下文,作用域整个流水线
   * */
  private String pipelineContext;

  /**
   * 记录状态
   * */
  private Integer status;

  private Long createTime;

  private Long updateTime;

}
