package com.zj.common.exception;

import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/6/21
 */
public class ExecuteException extends CommonException {

  private String message;

  public ExecuteException(ErrorCode errorCode) {
    super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    this.message = errorCode.getMessage();

  }

  public ExecuteException(String message) {
    super();
    this.message = "execute error: " + message;
  }

  @Override
  public String getMessage() {
    if (Objects.isNull(getMsg())) {
      return message;
    }

    return getMsg();
  }
}
