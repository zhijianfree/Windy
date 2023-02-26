package com.zj.common.exception;

/**
 * @author falcon
 * @since 2021/9/28
 */
public class CommonException extends RuntimeException{

  private ErrorCode errorCode;

  public CommonException(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
