package com.zj.master.service;

import com.zj.common.entity.dto.MasterCollectDto;
import com.zj.common.adapter.monitor.collector.InstanceCollector;
import com.zj.common.adapter.monitor.collector.PhysicsCollect;
import com.zj.master.dispatch.IDispatchExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Service
public class MasterMonitor {

  @Value("${windy.master.version}")
  private String masterVersion;
  private final List<IDispatchExecutor> dispatchExecutors;

  public MasterMonitor(List<IDispatchExecutor> dispatchExecutors) {
    this.dispatchExecutors = dispatchExecutors;
  }

  public MasterCollectDto getInstanceInfo() {
    MasterCollectDto masterCollectDto = new MasterCollectDto();
    PhysicsCollect physics = InstanceCollector.collectPhysics();
    masterCollectDto.setPhysics(physics);
    int count = dispatchExecutors.stream().mapToInt(IDispatchExecutor::getExecuteCount).sum();
    masterCollectDto.setTaskCount(count);
    masterCollectDto.setVersion(masterVersion);
    return masterCollectDto;
  }
}
