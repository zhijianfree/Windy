package com.zj.master.entity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@Data
@Builder
public class TestRequestContext extends RequestContext{

  private String taskId;
}
