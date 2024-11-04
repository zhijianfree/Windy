package com.zj.client.entity.bo;

import lombok.Data;

import java.util.List;

@Data
public class NodeRecord {

  private Long id;

  /**
   * 节点ID
   */
  private String nodeId;

  /**
   * 节点执行记录ID
   */
  private String recordId;

  /**
   * 流水线历史ID
   */
  private String historyId;

  private Integer code;

  /**
   * 节点执行过程描述
   */
  private List<String> result;

  /**
   * 执行状态
   */
  private Integer status;

}
