package com.zj.client.handler.pipeline.executer.vo;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
public class TriggerContext {

  private Object data;

  private TaskNode taskNode;

  public TriggerContext(Object data) {
    this.data = data;
  }

  public TriggerContext(Object data, TaskNode taskNode) {
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
