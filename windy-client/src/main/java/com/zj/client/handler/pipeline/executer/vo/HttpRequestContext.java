package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;

import java.util.Map;

/**
 * @author guyuelan
 * @since 2022/5/25
 */
@Data
public class HttpRequestContext {

  private String url;

  private String body;

  private Map<String, String> headers;
}
