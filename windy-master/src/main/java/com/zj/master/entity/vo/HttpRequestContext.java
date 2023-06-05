package com.zj.master.entity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2022/5/25
 */
@Data
@Builder
public class HttpRequestContext extends RequestContext {

  private String url;

  private String body;
}
