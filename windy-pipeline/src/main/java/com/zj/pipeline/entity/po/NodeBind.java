package com.zj.pipeline.entity.po;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/3/28
 */
@Data
public class NodeBind {

  private Long id;

  private String nodeId;

  private String nodeName;

  private String description;

  private String userId;
  private Long createTime;
  private Long updateTime;
}
