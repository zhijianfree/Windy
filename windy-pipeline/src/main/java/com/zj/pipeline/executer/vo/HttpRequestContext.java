package com.zj.pipeline.executer.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author falcon
 * @since 2022/5/25
 */
@Data
@Builder
public class HttpRequestContext extends RequestContext {

  private String url;

  private String body;
}
