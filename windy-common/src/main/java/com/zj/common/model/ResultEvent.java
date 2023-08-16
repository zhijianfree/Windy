package com.zj.common.model;

import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import lombok.Data;

import java.util.Map;

/**
 * @author guyuelan
 * @since 2023/5/16
 */
@Data
public class ResultEvent {

  private String logId;
  private String masterIP;
  private String executeType;
  private String clientIp;
  private String executeId;
  private NotifyType notifyType;
  private ProcessStatus status;

  private Map<String, Object> context;
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

  public ResultEvent executeType(String executeType) {
    this.executeType = executeType;
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

  public ResultEvent logId(String logId) {
    this.logId = logId;
    return this;
  }

  public ResultEvent clientIp(String clientIp) {
    this.clientIp = clientIp;
    return this;
  }

  public ResultEvent context(Map<String, Object> context) {
    this.context = context;
    return this;
  }
}
