package com.zj.common;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/3/29
 */
@Data
public class ResponseStatusModel {

  private Integer status;
  private Object data;
  private String message;

  public ResponseStatusModel() {
  }

  public ResponseStatusModel(Object data, String message) {
    this.data = data;
    this.message = message;
  }
}
