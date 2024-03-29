package com.zj.common.exception;

import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/6/21
 */
public class ExecuteException extends CommonException {

  private String message;

  public ExecuteException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ExecuteException(String message) {
    super();
    this.message = "execute error: " + message;
  }

  @Override
  public String getMessage() {
    if (Objects.isNull(getErrorCode())) {
      return message;
    }

    return getErrorCode().getMessage();
  }
}
