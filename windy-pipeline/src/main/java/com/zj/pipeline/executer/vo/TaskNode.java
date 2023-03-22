package com.zj.pipeline.executer.vo;

import lombok.Data;

/**
 * @author falcon
 * @since 2022/5/23
 */
@Data
public class TaskNode {
  private String nodeId;
  private String name;

  private String executeType;

  private RequestContext requestContext;

  private RefreshContext refreshContext;

  private NodeConfig nodeConfig;
}
