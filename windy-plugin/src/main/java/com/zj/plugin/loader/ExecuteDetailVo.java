package com.zj.plugin.loader;


import java.util.HashMap;
import java.util.Map;

public class ExecuteDetailVo {

  /**
   * 当前任务执行的请求信息与请求参数
   */
  private Map<String, Object> requestTips = new HashMap<String,Object>();

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

  public void addRequestInfo(String key, Object value) {
    requestTips.put(key, value);
  }

  public Map<String, Object> getRequestTips() {
    return requestTips;
  }

  public void setRequestTips(Map<String, Object> requestTips) {
    this.requestTips = requestTips;
  }

  public Boolean responseStatus() {
    return responseDetailVo.getResponseStatus();
  }

  public Object responseBody() {
    return responseDetailVo.getResponseBody();
  }

  public ResponseDetailVo getResponseDetailVo() {
    return responseDetailVo;
  }
}
