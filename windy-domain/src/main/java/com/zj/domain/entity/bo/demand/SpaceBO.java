package com.zj.domain.entity.bo.demand;

import lombok.Data;

@Data
public class SpaceBO {

  private Long id;

  private String spaceId;

  private String spaceName;

  private String description;

  private String userId;

  private Long createTime;

  private Long updateTime;
}
