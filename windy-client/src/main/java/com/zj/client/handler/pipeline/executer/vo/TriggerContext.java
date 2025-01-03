package com.zj.client.handler.pipeline.executer.vo;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
public class TriggerContext {

  private Object data;

  public TriggerContext(Object data) {
    this.data = data;
  }

  public Object getData() {
    return data;
  }

}
