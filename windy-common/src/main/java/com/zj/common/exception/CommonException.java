package com.zj.common.exception;

import lombok.Getter;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
@Getter
public class CommonException extends RuntimeException{

  private ErrorCode errorCode;

  public CommonException() {
  }

  public CommonException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

}
