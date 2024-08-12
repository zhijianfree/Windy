package com.zj.domain.entity.po.demand;

import lombok.Data;

@Data
public class Space {

  private Long id;

  /**
   * 空间ID
   */
  private String spaceId;

  /**
   * 空间名称
   */
  private String spaceName;


  /**
   * 空间描述
   */
  private String description;

  private Long createTime;

  private Long updateTime;
}
