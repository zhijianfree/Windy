package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author guyuelan
 * @since 2022/6/15
 */
@Data
public class RefreshContext {

  /**
   * 刷新的URL
   */
  private String url;

  /**
   * 循环查询的条件
   */
  private CompareInfo loopExpression;

  /**
   * 状态查询的Header信息
   */
  private Map<String, String> headers;

  /**
   * 刷新状态结果比对条件
   */
  private List<CompareInfo> compareConfig;

  /**
   * 触发任务的记录ID(在Request的trigger触发的任务关联ID)
   */
  private String recordId;

  public RefreshContext() {
  }

  public RefreshContext(String url, Map<String, String> headers,
      List<CompareInfo> compareConfig) {
    this.url = url;
    this.headers = headers;
    this.compareConfig = compareConfig;
  }
}
