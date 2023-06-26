package com.zj.common.monitor.log.dto;

/**
 * @author falcon
 * @since 2021/11/5
 */
public enum LoggerType {
  LOG4J("org.slf4j.impl.Log4jLoggerFactory"),
  LOG4J2("org.apache.logging.slf4j.Log4jLoggerFactory"),
  LOGBACK("ch.qos.logback.classic.util.ContextSelectorStaticBinder"),
  UNKNOWN("UNKNOWN"),;

  private String logName;

  LoggerType(String logName) {
    this.logName = logName;
  }

  public String getLogName() {
    return logName;
  }
}
