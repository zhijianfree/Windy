package com.zj.common.entity.dto;

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
  /**
   * 执行任务的日志ID
   * */
  private String logId;

  /**
   * 分配任务的主节点IP
   */
  private String masterIP;

  /**
   * 执行类型
   */
  private String executeType;

  /**
   * 实际实行的client节点IP
   */
  private String clientIp;

  /**
   * 任务ID（流水线Id、用例Id、任务Id）
   */
  private String executeId;

  /**
   * 结果通知的类型
   */
  private NotifyType notifyType;

  /**
   * 任务状态
   */
  private ProcessStatus status;

  /**
   * 执行完成之后需要存储的临时上下文。这个上下文主要是为了任务的后续子任务使用，用来存储运行过程中的临时全局变量
   */
  private Map<String, Object> context;

  /**
   * 用来存放任务详细信息
   */
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
