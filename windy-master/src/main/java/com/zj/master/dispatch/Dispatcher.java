package com.zj.master.dispatch;

import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.domain.entity.bo.log.DispatchLogBO;
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

  private final Map<Integer, IDispatchExecutor> dispatchExecutorMap;

  public Dispatcher(List<IDispatchExecutor> dispatchExecutors) {
    this.dispatchExecutorMap = dispatchExecutors.stream()
        .collect(Collectors.toMap(executor -> executor.type().getType(), dispatchExecutor -> dispatchExecutor));
  }

  public Object dispatch(DispatchTaskModel task, String logId) {
    log.info("go into dispatch name={}", task.getSourceName());
    IDispatchExecutor dispatchExecutor = dispatchExecutorMap.get(task.getType());
    if (Objects.isNull(dispatchExecutor)) {
      log.info("can not find dispatch executor");
      return null;
    }
    log.info("start dispatch name={}", task.getSourceName());
    return dispatchExecutor.dispatch(task, logId);
  }

  public boolean resumeTask(DispatchLogBO taskLog) {
    log.info("start resume task type={}", taskLog.getLogType());
    try {
      IDispatchExecutor dispatchExecutor = dispatchExecutorMap.get(taskLog.getLogType());
      return dispatchExecutor.resume(taskLog);
    }catch (Exception e){
      log.info("resume task error", e);
    }
    return false;
  }

  public boolean isExitInJvm(DispatchLogBO taskLog){
    IDispatchExecutor dispatchExecutor = dispatchExecutorMap.get(taskLog.getLogType());
    return dispatchExecutor.isExistInJvm(taskLog);
  }
}
