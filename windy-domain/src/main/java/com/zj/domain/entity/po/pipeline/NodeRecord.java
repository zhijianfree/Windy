package com.zj.domain.entity.po.pipeline;

import lombok.Data;

@Data
public class NodeRecord {

  private Long id;

  private String nodeId;

  private String recordId;

  private String historyId;

  private Integer code;

  private String result;

  private Integer status;

  private Long createTime;

  private Long updateTime;

}
