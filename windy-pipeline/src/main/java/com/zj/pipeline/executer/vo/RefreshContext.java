package com.zj.pipeline.executer.vo;

import java.util.Map;
import lombok.Data;

/**
 * @author falcon
 * @since 2022/6/15
 */
@Data
public class RefreshContext {

  private String url;

  private Map<String, String> headers;
}
