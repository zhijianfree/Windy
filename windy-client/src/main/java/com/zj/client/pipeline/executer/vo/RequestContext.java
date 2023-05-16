package com.zj.client.pipeline.executer.vo;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
public class RequestContext {

  private Object data;

  public RequestContext(Object data) {
    this.data = data;
  }

  public Object getData() {
    return data;
  }
}
