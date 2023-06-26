package com.zj.common.monitor.log.impl;

import com.google.common.collect.Lists;
import com.zj.common.monitor.log.ILogger;
import com.zj.common.monitor.log.dto.UpdateLevel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * @author falcon
 * @since 2021/11/5
 */
@Slf4j
public abstract class BaseLogger implements ILogger {

  protected Map<String, String> loggerMap = new ConcurrentHashMap<>();

  @Override
  public void reset() {
    List<UpdateLevel> updateLevels = Lists.newArrayList();
    loggerMap.keySet().forEach(key -> {
      UpdateLevel updateLevel = new UpdateLevel();
      String level = loggerMap.get(key);
      updateLevel.setLogName(key);
      updateLevel.setLevel(level);
      updateLevels.add(updateLevel);
    });

    log.info("reset logger level {}", updateLevels);
    setLevel(updateLevels);
  }
}
