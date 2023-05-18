package com.zj.master.dispatch;

import com.zj.master.entity.dto.TaskDetailDto;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
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

  public Boolean dispatch(TaskDetailDto task) {
    log.info("go into dispatch name={}", task.getSourceName());
    IDispatchExecutor dispatchExecutor = dispatchExecutorMap.get(task.getType());
    if (Objects.isNull(dispatchExecutor)) {
      log.info("can not find dispatch executor");
      return false;
    }
    log.info("start dispatch name={}", task.getSourceName());
    return dispatchExecutor.dispatch(task);
  }
}
