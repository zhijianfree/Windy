package com.zj.common.enums;

public enum NotifyType {
  CREATE_GENERATE_MAVEN("创建Maven推送记录"),
  UPDATE_GENERATE_MAVEN("修改Maven推送记录"),
  CREATE_FEATURE_HISTORY("创建用例历史记录"),
  UPDATE_FEATURE_HISTORY("修改用例历史记录"),
  CREATE_EXECUTE_POINT_RECORD("创建执行点运行记录"),
  UPDATE_NODE_RECORD("更新流水线节点记录"),
  CREATE_NODE_RECORD("创建节点记录");

  private final String desc;

  NotifyType(String desc) {
    this.desc = desc;
  }
}
