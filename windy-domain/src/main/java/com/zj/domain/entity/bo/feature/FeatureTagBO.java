package com.zj.domain.entity.bo.feature;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/1/28
 */
@Data
public class FeatureTagBO {

  private Long id;

  /**
   * 用例ID
   */
  private String featureId;

  /**
   * 标签值
   */
  private String tagValue;

  /**
   * 创建时间
   */
  private Long createTime;
}
