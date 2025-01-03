package com.zj.master.entity.vo;

import com.zj.common.entity.pipeline.CompareParameter;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author guyuelan
 * @since 2022/6/15
 */
@Data
@Builder
public class RefreshContext {

  private String url;

  private CompareParameter loopExpression;

  private Map<String, String> headers;

  private List<CompareParameter> compareConfig;
}
