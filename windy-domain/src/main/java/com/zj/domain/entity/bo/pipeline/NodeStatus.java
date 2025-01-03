package com.zj.domain.entity.bo.pipeline;

import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/4/6
 */
@Data
public class NodeStatus {

  private String nodeId;

  private String recordId;

  private Integer status;

  private List<String> message;
}
