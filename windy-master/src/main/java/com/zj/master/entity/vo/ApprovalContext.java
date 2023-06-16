package com.zj.master.entity.vo;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Data
public class ApprovalContext extends RequestContext{

  /**
   * 审批最大等待时长(秒)
   * */
  private Integer maxWait;
}
