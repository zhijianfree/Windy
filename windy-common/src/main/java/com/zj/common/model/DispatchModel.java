package com.zj.common.model;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/5/18
 */
@Data
public class DispatchModel {

  private String sourceId;

  private String sourceName;

  private Integer type;

  public DispatchModel() {
  }

  public DispatchModel(String sourceId, String sourceName, Integer type) {
    this.sourceId = sourceId;
    this.sourceName = sourceName;
    this.type = type;
  }
}
