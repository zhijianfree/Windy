package com.zj.domain.entity.dto.demand;

import lombok.Data;

@Data
public class SpaceDTO {

  private Long id;

  private String spaceId;

  private String spaceName;

  private String description;

  private String userId;

  private Long createTime;

  private Long updateTime;
}
