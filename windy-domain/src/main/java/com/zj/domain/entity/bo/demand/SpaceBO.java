package com.zj.domain.entity.bo.demand;

import lombok.Data;

@Data
public class SpaceBO {

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
   * 描述
   */
  private String description;

  /**
   * 创建空间用户Id
   */
  private String userId;

  /**
   * 创建时间
   */
  private Long createTime;

  /**
   * 修改时间
   */
  private Long updateTime;
}
