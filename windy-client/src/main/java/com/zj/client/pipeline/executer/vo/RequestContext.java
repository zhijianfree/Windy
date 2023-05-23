package com.zj.client.pipeline.executer.vo;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
public class RequestContext {

  private Object data;

  private TaskNode taskNode;

  public RequestContext(Object data) {
    this.data = data;
  }

  public RequestContext(Object data, TaskNode taskNode) {
    this.data = data;
    this.taskNode = taskNode;
  }

  public Object getData() {
    return data;
  }

  public TaskNode getTaskNode() {
    return taskNode;
  }
}
