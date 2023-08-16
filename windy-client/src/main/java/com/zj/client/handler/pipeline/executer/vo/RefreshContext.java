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

  private String url;

  private Map<String, String> headers;

  private List<CompareInfo> compareConfig;

  public RefreshContext() {
  }

  public RefreshContext(String url, Map<String, String> headers,
      List<CompareInfo> compareConfig) {
    this.url = url;
    this.headers = headers;
    this.compareConfig = compareConfig;
  }
}
