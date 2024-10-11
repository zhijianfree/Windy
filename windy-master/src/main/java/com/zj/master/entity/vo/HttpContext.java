package com.zj.master.entity.vo;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author guyuelan
 * @since 2022/5/25
 */
@Data
@Builder
public class HttpContext extends RequestContext {

  private String url;

  private String body;

  private Map<String, String> headers;
}
