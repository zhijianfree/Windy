package com.zj.domain.entity.dto.pipeline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author guyuelan
 * @since 2023/3/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
