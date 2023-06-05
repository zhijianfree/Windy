package com.zj.domain.entity.dto.pipeline;

import lombok.Data;

@Data
public class NodeRecordDto {

  private String nodeId;

  private String recordId;

  private String historyId;

  private Integer code;

  private String result;

  private Integer status;

}
