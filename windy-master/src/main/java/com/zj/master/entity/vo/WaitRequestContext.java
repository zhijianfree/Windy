package com.zj.master.entity.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Data
@Builder
public class WaitRequestContext extends RequestContext{

  /**
   * 等待时长，单位s
   * */
  private Integer waitTime;
}
