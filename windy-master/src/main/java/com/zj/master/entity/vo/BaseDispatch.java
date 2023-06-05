package com.zj.master.entity.vo;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/16
 */
@Data
public class BaseDispatch {
  /**
   * 分发类型
   * */
  private String dispatchType;

  /**
   * 执行任务的master节点IP
   * */
  private String masterIp;
}
