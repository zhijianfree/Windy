package com.zj.common.monitor.log;

import com.alibaba.fastjson.JSON;
import com.zj.common.monitor.log.dto.UpdateLevel;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @author falcon
 * @since 2021/11/5
 */
@Slf4j
@Component
public class LogHandler {

  public static final String SERVICE_LOG_LEVEL_CONFIG = "service.log.level";
  public static final String LOG_LEVEL_LIST = "log.level.list";
  private final Map<String, ILogger> loggerMap;
  private ILogger logger;

  public LogHandler(List<ILogger> loggers) {
    this.loggerMap = loggers.stream().collect(
        Collectors.toMap(iLogger -> iLogger.getLogType().getLogName(), iLogger -> iLogger));
  }

  @PostConstruct
  public void init() {
    String type = StaticLoggerBinder.getSingleton().getLoggerFactoryClassStr();
    logger = loggerMap.get(type);
    if (Objects.isNull(logger)) {
      log.error("logger is null  {}", type);
      return;
    }
    logger.init();

    initApollo();
  }

  private void initApollo() {
    //todo 后面集成apollo后可以开启动态日志级别开关
//    Config config = ConfigService.getConfig(SERVICE_LOG_LEVEL_CONFIG);
//    updateLogLevel(config.getProperty(LOG_LEVEL_LIST, ""));
//
//    config.addChangeListener(configChangeEvent -> {
//      log.info("start get apollo update config");
//      updateLogLevel(configChangeEvent.getChange(LOG_LEVEL_LIST).getNewValue());
//    });
  }

  private void updateLogLevel(String logConfigString) {
    List<UpdateLevel> updateLevels = JSON.parseArray(logConfigString, UpdateLevel.class);
    if (CollectionUtils.isEmpty(updateLevels)) {
      log.info("update list is empty,reset loggers level");
      logger.reset();
      return;
    }

    logger.setLevel(updateLevels);
  }


}
