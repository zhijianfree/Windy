package com.zj.common.exception;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
public class CommonException extends RuntimeException{

  private ErrorCode errorCode;

  public CommonException() {
  }

  public CommonException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }



  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
