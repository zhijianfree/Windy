package com.zj.domain.entity.po.demand;

import lombok.Data;

@Data
public class Iteration {

  private Long id;

  /**
   * 迭代ID
   */
  private String iterationId;

  private String name;

  /**
   * 迭代描述
   */
  private String description;

  /**
   * 开始时间
   */
  private Long startTime;

  /**
   * 结束时间
   */
  private Long endTime;

  /**
   * 迭代状态
   */
  private Integer status;

  /**
   * 创建迭代用户
   */
  private String userId;

  /**
   * 空间ID
   */
  private String spaceId;

  private Long createTime;

  private Long updateTime;
}
