package com.zj.pipeline.entity.dto;

import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/4/6
 */
@Data
public class NodeStatus {

  private String nodeId;

  private String recordId;

  private Integer status;

  private List<String> message;
}
