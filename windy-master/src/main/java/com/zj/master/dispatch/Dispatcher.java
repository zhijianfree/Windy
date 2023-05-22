package com.zj.master.dispatch;

import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.master.entity.dto.TaskDetailDto;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

  public boolean dispatch(TaskDetailDto task) {
    log.info("go into dispatch name={}", task.getSourceName());
    IDispatchExecutor dispatchExecutor = dispatchExecutorMap.get(task.getType());
    if (Objects.isNull(dispatchExecutor)) {
      log.info("can not find dispatch executor");
      return false;
    }
    log.info("start dispatch name={}", task.getSourceName());
    return dispatchExecutor.dispatch(task);
  }

  public boolean resumeTask(DispatchLogDto taskLog) {
    log.info("start resume task type={}", taskLog.getLogType());
    IDispatchExecutor dispatchExecutor = dispatchExecutorMap.get(taskLog.getLogType());
    return dispatchExecutor.resume(taskLog);
  }
}
