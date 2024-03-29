package com.zj.common.enums;

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
  TEST,
  /**
   * 部署代码
   * */
  DEPLOY,
  /**
   * 构建代码
   * */
  BUILD,
  /**
   * 合并代码到master
   * */
  MERGE
}
