package com.zj.client.entity.vo;

import lombok.Data;

import java.util.List;

@Data
public class NodeRecord {

  private Long id;

  private String nodeId;

  private String recordId;

  private String historyId;

  private Integer code;

  private List<String> result;

  private Integer status;

}
