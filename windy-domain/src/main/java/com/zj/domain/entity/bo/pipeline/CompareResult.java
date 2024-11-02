package com.zj.domain.entity.bo.pipeline;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Data
public class CompareResult {

  private String compareKey;

  private String description;

  private String operator;

  private String valueType;

  private String value;

  private boolean showCompare;
}
