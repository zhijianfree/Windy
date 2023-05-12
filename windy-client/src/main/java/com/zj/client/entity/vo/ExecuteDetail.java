package com.zj.client.entity.vo;

import lombok.Data;

@Data
public class ExecuteDetail {

  /**
   * 当前任务执行的请求信息与请求参数
   */
  private RequestDetail requestDetail = new RequestDetail();

  /**
   * 当前请求执行完成之后的响应结果
   */
  private ResponseDetail responseDetail = new ResponseDetail();

  public void setStatus(Boolean code) {
    responseDetail.setResponseStatus(code);
  }

  public void setResBody(Object responseBody) {
    responseDetail.setResponseBody(responseBody);
  }

  public void setErrorMessage(String errorMessage) {
    responseDetail.setErrorMessage(errorMessage);
  }

  public void addRequestInfo(String info) {
    requestDetail.getRequest().add(info);
  }

  public void setRequestBody(Object requestBody) {
    requestDetail.setRequestBody(requestBody);
  }

  public Boolean responseStatus() {
    return responseDetail.getResponseStatus();
  }

  public Object responseBody() {
    return responseDetail.getResponseBody();
  }
}
