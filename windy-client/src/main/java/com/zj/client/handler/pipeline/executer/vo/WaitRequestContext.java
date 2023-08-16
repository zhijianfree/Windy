package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Data
public class WaitRequestContext{

  /**
   * 等待时长，单位s
   * */
  private Integer waitTime;
}
