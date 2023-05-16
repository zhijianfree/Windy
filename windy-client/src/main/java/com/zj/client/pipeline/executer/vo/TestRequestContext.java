package com.zj.client.pipeline.executer.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@Data
public class TestRequestContext{

  private String taskId;

  public TestRequestContext(String taskId) {
    this.taskId = taskId;
  }
}
