package com.zj.client.pipeline.executer.vo;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/6/15
 */
@Data
public class RefreshContext {

  private String url;

  private Map<String, String> headers;

  private List<CompareResult> compareConfig;

  public RefreshContext() {
  }

  public RefreshContext(String url, Map<String, String> headers,
      List<CompareResult> compareConfig) {
    this.url = url;
    this.headers = headers;
    this.compareConfig = compareConfig;
  }
}
