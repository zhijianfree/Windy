package com.zj.pipeline.entity.po;

import lombok.Data;

@Data
public class NodeRecord {

  private Long id;

  private String recordId;

  private String taskId;

  private Integer code;

  private String result;

  private Integer status;

  private Long createTime;

  private Long updateTime;

}
