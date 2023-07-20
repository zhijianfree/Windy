package com.zj.client.entity.dto;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Data
public class LoopQueryResponse {

  private Integer status;
  private Object data;
  private String message;

  public LoopQueryResponse() {
  }

  public LoopQueryResponse(Integer status, String message) {
    this.status = status;
    this.message = message;
  }

  public LoopQueryResponse(Object data, String message) {
    this.data = data;
    this.message = message;
  }

  @Data
  public static class ResponseStatus{

    private Integer status;
  }
}
