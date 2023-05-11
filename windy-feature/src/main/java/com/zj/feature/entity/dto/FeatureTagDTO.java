package com.zj.feature.entity.dto;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/1/28
 */
@Data
public class FeatureTagDTO {
  private String featureId;

  private String tagValue;

  private Long createTime;
}
