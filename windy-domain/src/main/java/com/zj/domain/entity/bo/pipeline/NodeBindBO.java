package com.zj.domain.entity.bo.pipeline;

import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/3/28
 */
@Data
public class NodeBindBO {

  private Long id;

  /**
   * 流水线执行点ID
   */
  private String nodeId;

  /**
   * 流水线执行点名称
   */
  private String nodeName;

  /**
   * 流水线执行点描述
   */
  private String description;

  private List<String> executors;

  /**
   * 创建人
   */
  private String userId;

  /**
   * 创建时间
   */
  private Long createTime;

  /**
   * 更新时间
   */
  private Long updateTime;
}
