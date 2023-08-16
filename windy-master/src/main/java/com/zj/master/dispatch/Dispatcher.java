package com.zj.master.dispatch;

import com.zj.common.model.DispatchTaskModel;
import com.zj.domain.entity.dto.log.DispatchLogDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/11
 */
@Slf4j
@Component
public class Dispatcher {

  private Map<Integer, IDispatchExecutor> dispatchExecutorMap;

  public Dispatcher(List<IDispatchExecutor> dispatchExecutors) {
    this.dispatchExecutorMap = dispatchExecutors.stream()
        .collect(Collectors.toMap(IDispatchExecutor::type, dispatchExecutor -> dispatchExecutor));
  }

  public String dispatch(DispatchTaskModel task, String logId) {
    log.info("go into dispatch name={}", task.getSourceName());
    IDispatchExecutor dispatchExecutor = dispatchExecutorMap.get(task.getType());
    if (Objects.isNull(dispatchExecutor)) {
      log.info("can not find dispatch executor");
      return null;
    }
    log.info("start dispatch name={}", task.getSourceName());
    return dispatchExecutor.dispatch(task, logId);
  }

  public boolean resumeTask(DispatchLogDto taskLog) {
    log.info("start resume task type={}", taskLog.getLogType());
    IDispatchExecutor dispatchExecutor = dispatchExecutorMap.get(taskLog.getLogType());
    return dispatchExecutor.resume(taskLog);
  }

  public boolean isExitInJvm(DispatchLogDto taskLog){
    IDispatchExecutor dispatchExecutor = dispatchExecutorMap.get(taskLog.getLogType());
    return dispatchExecutor.isExitInJvm(taskLog);
  }
}
