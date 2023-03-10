package com.zj.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  /*===================流水线=================*/
  SUCCESS(HttpStatus.OK, "Pipeline.000000", "请求成功"),
  ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"Pipeline.000001", "请求失败"),
  PARAM_VALIDATE_ERROR(HttpStatus.BAD_REQUEST,"Pipeline.000005", "参数校验失败"),
  NOT_FOUND_PIPELINE(HttpStatus.NOT_FOUND,"Pipeline.000002", "流水线未找到"),
  NOT_FOUND_CODE_CHANGE(HttpStatus.NOT_FOUND,"Pipeline.000003", "服务变更未找到"),
  NOT_FOUND_PIPELINE_GIT_BIND(HttpStatus.NOT_FOUND,"Pipeline.000004", "流水关联分支未找到"),
  UPDATE_PIPELINE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"Pipeline.000005", "更新流水线失败"),

  /*==================用例服务==================*/
  COMPARE_ERROR(HttpStatus.FORBIDDEN,"Feature.000002","feature compare error"),
  EXECUTE_POINT_NOT_FIND(HttpStatus.NOT_FOUND,"Feature.000003","can not find execute point"),
  FEATURE_NOT_FIND(HttpStatus.NOT_FOUND,"Feature.000004","can not find feature"),
  SYSTEM_EXECUTE_ERROR(HttpStatus.BAD_REQUEST,"Feature.000101","unknown error occur when execute feature "),
  SUB_FEATURE_EXIST(HttpStatus.BAD_REQUEST, "Feature.000005", "存在子用例不能删除目录");

  ErrorCode(HttpStatus httpStatus,String code, String message) {
    this.code = code;
    this.message = message;
    this.httpStatus = httpStatus;
  }

  private String code;
  private String message;
  private HttpStatus httpStatus;

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public void bindMessage(String message) {
    this.message = this.message + " [" + message + "]";
  }
}
