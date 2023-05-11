package com.zj.pipeline.entity.dto;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/3/10
 */
@Data
public class RelationDemandBug {

  /**
   * 关联的需求或者缺陷ID
   * */
  private String relationId;

  /**
   * 关联类型 1 需求 2 bug
   * */
  private Integer relationType;

  /**
   * 需求或者缺陷名称
   * */
  private String name;
}