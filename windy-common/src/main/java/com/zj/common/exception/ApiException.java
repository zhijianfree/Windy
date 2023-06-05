package com.zj.common.exception;

/**
 * @author guyuelan
 * @since 2021/9/28
 */
public class ApiException extends CommonException{

  public ApiException(ErrorCode errorCode) {
    super(errorCode);
  }
}
