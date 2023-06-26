package com.zj.common.monitor.log.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.zj.common.monitor.log.dto.LoggerType;
import com.zj.common.monitor.log.dto.UpdateLevel;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2021/11/5
 */
@Slf4j
@Component
public class LogbackLogger extends BaseLogger {

  private final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

  @Override
  public LoggerType getLogType() {
    return LoggerType.LOGBACK;
  }

  public void init() {
  }

  @Override
  public void setLevel(List<UpdateLevel> updateLevels) {
    for (UpdateLevel updateLevel : updateLevels) {
      Logger targetLogger = loggerContext.getLogger(updateLevel.getLogName());

      storeLoggerHistoryLevel(updateLevel, targetLogger);

      Level targetLevel = Level.toLevel(updateLevel.getLevel());
      targetLogger.setLevel(targetLevel);
      log.info("update log {} level = {} current={}", updateLevel.getLogName(),
          updateLevel.getLevel(), targetLogger.getLevel());
    }
  }

  @Override
  public void reset() {
    super.reset();
    loggerMap.clear();
  }

  /**
   * 记录每次修改的logger和日志级别，清除的时候使用
   */
  private void storeLoggerHistoryLevel(UpdateLevel updateLevel, Logger targetLogger) {
    Level level = targetLogger.getEffectiveLevel();
    if (Objects.nonNull(level)) {
      log.info("store logger Level {} {}", updateLevel.getLogName(), level);
      loggerMap.put(updateLevel.getLogName(), level.toString());
    }
  }
}
