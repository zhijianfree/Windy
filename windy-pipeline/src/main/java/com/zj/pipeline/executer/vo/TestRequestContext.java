package com.zj.pipeline.executer.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/5/9
 */
@Data
@Builder
public class TestRequestContext extends RequestContext{

  private String taskId;
}
