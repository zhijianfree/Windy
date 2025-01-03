package com.zj.common.entity.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Data
public class CompareParameter {

  private String compareKey;

  private String description;

  private String operator;

  private String valueType;

  private String value;

  private boolean showCompare;
}
