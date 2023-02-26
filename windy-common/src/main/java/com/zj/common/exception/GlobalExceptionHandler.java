package com.zj.common.exception;

import com.zj.common.ResponseMeta;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author falcon
 * @since 2021/9/28
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  public static final String LEFT_STRING = "[";
  public static final String RIGHT_STRING = "] ";

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseEntity<ResponseMeta> handleException(Exception e) {
    ErrorCode errorCode = ErrorCode.ERROR;
    errorCode.bindMessage(e.getMessage());

    if (e instanceof MethodArgumentNotValidException) {
      errorCode = resetParamErrorMsg(e);
    }

    log.warn("param validate error", e);
    return new ResponseEntity<ResponseMeta>(new ResponseMeta(errorCode),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ErrorCode resetParamErrorMsg(Exception e) {
    List<FieldError> errors = ((MethodArgumentNotValidException) e)
        .getBindingResult().getFieldErrors();
    StringBuilder stringBuilder = new StringBuilder();
    errors.forEach(fieldError -> {
      stringBuilder.append(LEFT_STRING).append(fieldError.getField()).append(RIGHT_STRING)
          .append(fieldError.getDefaultMessage());
    });

    ErrorCode errorCode = ErrorCode.PARAM_VALIDATE_ERROR;
    errorCode.bindMessage(stringBuilder.toString());
    return errorCode;
  }

  @ExceptionHandler(CommonException.class)
  @ResponseBody
  public ResponseEntity<ResponseMeta> handleBusinessException(CommonException commonException) {
    log.warn("common error", commonException);
    return new ResponseEntity<ResponseMeta>(new ResponseMeta(commonException.getErrorCode()),
        commonException.getErrorCode().getHttpStatus());
  }
}
