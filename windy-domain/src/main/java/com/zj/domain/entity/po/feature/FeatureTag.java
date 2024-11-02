package com.zj.domain.entity.po.feature;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/1/28
 */
@Data
public class FeatureTag {

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
