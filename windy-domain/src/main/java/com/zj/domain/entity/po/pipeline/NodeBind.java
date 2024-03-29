package com.zj.domain.entity.po.pipeline;

import lombok.Data;

/**
 * @author guyuelan
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
