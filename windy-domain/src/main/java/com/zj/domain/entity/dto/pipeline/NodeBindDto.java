package com.zj.domain.entity.dto.pipeline;

import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/3/28
 */
@Data
public class NodeBindDto {

  private String nodeId;

  private String nodeName;

  private String description;

  private List<String> executors;

  private Long createTime;

  private Long updateTime;
}
