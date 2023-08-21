package com.zj.plugin.loader;


public class ExecuteDetailVo {

  /**
   * 当前任务执行的请求信息与请求参数
   */
  private RequestDetailVo requestDetailVo = new RequestDetailVo();

  /**
   * 当前请求执行完成之后的响应结果
   */
  private ResponseDetailVo responseDetailVo = new ResponseDetailVo();

  public void setStatus(Boolean status) {
    responseDetailVo.setResponseStatus(status);
  }

  public void setResBody(Object responseBody) {
    responseDetailVo.setResponseBody(responseBody);
  }

  public void setErrorMessage(String errorMessage) {
    responseDetailVo.setErrorMessage(errorMessage);
  }

  public void addRequestInfo(String info) {
    requestDetailVo.addTips(info);
  }

  public void setRequestBody(Object requestBody) {
    requestDetailVo.setRequestBody(requestBody);
  }

  public Boolean responseStatus() {
    return responseDetailVo.getResponseStatus();
  }

  public Object responseBody() {
    return responseDetailVo.getResponseBody();
  }

  public RequestDetailVo getRequestDetailVo() {
    return requestDetailVo;
  }

  public ResponseDetailVo getResponseDetailVo() {
    return responseDetailVo;
  }
}
