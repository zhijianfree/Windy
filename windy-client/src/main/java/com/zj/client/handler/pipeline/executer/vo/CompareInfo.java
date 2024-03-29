package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Data
public class CompareInfo {

  private String compareKey;

  private String description;

  private String operator;

  private String valueType;

  private String value;
}
