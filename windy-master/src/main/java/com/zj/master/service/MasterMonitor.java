package com.zj.master.service;

import com.zj.common.entity.dto.MasterCollect;
import com.zj.common.adapter.monitor.collector.InstanceCollector;
import com.zj.common.adapter.monitor.collector.PhysicsCollect;
import com.zj.master.dispatch.IDispatchExecutor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Service
public class MasterMonitor {
  private final List<IDispatchExecutor> dispatchExecutors;

  public MasterMonitor(List<IDispatchExecutor> dispatchExecutors) {
    this.dispatchExecutors = dispatchExecutors;
  }

  public MasterCollect getInstanceInfo() {
    MasterCollect masterCollect = new MasterCollect();
    PhysicsCollect physics = InstanceCollector.collectPhysics();
    masterCollect.setPhysics(physics);
    int count = dispatchExecutors.stream().mapToInt(IDispatchExecutor::getExecuteCount).sum();
    masterCollect.setTaskCount(count);
    return masterCollect;
  }
}
