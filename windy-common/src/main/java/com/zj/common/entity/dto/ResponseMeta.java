package com.zj.common.entity.dto;

import com.zj.common.exception.ErrorCode;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Data
public class ResponseMeta<T> {

  private String code;

  private String message;

  private T data;

  public ResponseMeta(String code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  public ResponseMeta(ErrorCode code, T data) {
    this.code = code.getCode();
    this.message = code.getMessage();
    this.data = data;
  }

  public ResponseMeta(ErrorCode code) {
    this.code = code.getCode();
    this.message = code.getMessage();
  }
}
