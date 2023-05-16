package com.zj.client.pipeline.executer.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/5/25
 */
@Data
public class HttpRequestContext {

  private String url;

  private String body;
}
