package com.zj.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Getter
public class CommonException extends RuntimeException{

  private String code;
  private String msg;
  private HttpStatus httpStatus;

  public CommonException() {
  }

  public CommonException(String code, String msg, HttpStatus httpStatus) {
    super(msg);
    this.code = code;
    this.msg = msg;
    this.httpStatus = httpStatus;
  }
  public CommonException(ErrorCode errorCode, String content) {
    super(String.format(errorCode.getMessage(), content));
    this.code = errorCode.getCode();
    this.msg = String.format(errorCode.getMessage(), content);
    this.httpStatus = errorCode.getHttpStatus();
  }
}
