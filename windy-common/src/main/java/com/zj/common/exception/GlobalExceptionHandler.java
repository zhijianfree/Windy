package com.zj.common.exception;

import com.zj.common.entity.dto.ResponseMeta;
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
 * @author guyuelan
 * @since 2021/9/28
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  public static final String LEFT_STRING = "【";
  public static final String RIGHT_STRING = "】 ";

  @ExceptionHandler(Exception.class)
  @ResponseBody
  public ResponseEntity<ResponseMeta> handleException(Exception e) {
    String errorCode = ErrorCode.ERROR.getCode();
    String message = e.getMessage();
    if (e instanceof MethodArgumentNotValidException) {
      log.warn("param validate error", e);
      message = resetParamErrorMsg(e);
    }

    log.error("get error", e);
    return new ResponseEntity<>(new ResponseMeta(errorCode, message, null),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private String resetParamErrorMsg(Exception e) {
    List<FieldError> errors = ((MethodArgumentNotValidException) e)
        .getBindingResult().getFieldErrors();
    StringBuilder stringBuilder = new StringBuilder();
    errors.forEach(fieldError -> stringBuilder.append(LEFT_STRING).append(fieldError.getField()).append(RIGHT_STRING)
        .append(fieldError.getDefaultMessage()));
    return stringBuilder.toString();
  }

  @ExceptionHandler(CommonException.class)
  @ResponseBody
  public ResponseEntity<ResponseMeta> handleBusinessException(CommonException commonException) {
    log.warn("common error", commonException);
    return new ResponseEntity<>(new ResponseMeta(commonException.getErrorCode()),
        commonException.getErrorCode().getHttpStatus());
  }
}
