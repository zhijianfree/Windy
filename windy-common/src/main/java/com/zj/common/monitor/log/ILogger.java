package com.zj.common.monitor.log;

import com.zj.common.monitor.log.dto.LoggerType;
import com.zj.common.monitor.log.dto.UpdateLevel;
import java.util.List;

/**
 * @author guyuelan
 * @since 2021/11/5
 */
public interface ILogger {

  LoggerType getLogType();

  /**
   * 初始化的时候获取当前已经配置的logger，方便后面重制配置
   * */
  void init();

  /**
   * 修改日志级别
   * */
  void setLevel(List<UpdateLevel> updateLevels);

  /**
   * 回复日志启动时的日志级别
   * */
  void reset();
}
