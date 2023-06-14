package com.zj.client.entity.dto;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Data
public class ResponseModel {

  private Integer status;
  private Object data;
  private String message;

  public ResponseModel() {
  }

  public ResponseModel(Integer status, String message) {
    this.status = status;
    this.message = message;
  }

  public ResponseModel(Object data, String message) {
    this.data = data;
    this.message = message;
  }
}
