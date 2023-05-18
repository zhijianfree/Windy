package com.zj.common.model;

import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/16
 */
@Data
public class ResultEvent {
  private String masterIP;
  private String executeId;
  private NotifyType notifyType;
  private ProcessStatus status;
  private Object params;

  public ResultEvent masterIP(String masterIP) {
    this.masterIP = masterIP;
    return this;
  }

  public ResultEvent executeId(String executeId) {
    this.executeId = executeId;
    return this;
  }

  public ResultEvent notifyType(NotifyType notifyType) {
    this.notifyType = notifyType;
    return this;
  }

  public ResultEvent status(ProcessStatus status) {
    this.status = status;
    return this;
  }

  public ResultEvent params(Object params) {
    this.params = params;
    return this;
  }
}
