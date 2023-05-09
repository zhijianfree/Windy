package com.zj.pipeline.executer.vo;

public enum ExecuteType {
  /**
   * HTTP请求执行
   * */
  HTTP,
  /**
   * 等待执行
   * */
  WAIT,
  /**
   * 审批执行
   * */
  APPROVAL,
  /**
   * 执行测试用例任务
   * */
  TEST
}
