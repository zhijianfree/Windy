package com.zj.client.service;

import com.zj.client.handler.pipeline.executer.notify.NodeStatusQueryLooper;
import com.zj.common.adapter.monitor.collector.InstanceCollector;
import com.zj.common.adapter.monitor.collector.PhysicsCollect;
import com.zj.common.entity.dto.ClientCollect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Slf4j
@Service
public class ClientMonitor {

  public static final String PLUGINS_PATH = "plugins";
  private final NodeStatusQueryLooper nodeStatusQueryLooper;

  public ClientMonitor(NodeStatusQueryLooper nodeStatusQueryLooper) {
    this.nodeStatusQueryLooper = nodeStatusQueryLooper;
  }

  public ClientCollect getInstanceInfo() {
    ClientCollect clientCollect = new ClientCollect();
    PhysicsCollect physics = InstanceCollector.collectPhysics();
    clientCollect.setPhysics(physics);
    Integer waitQuerySize = nodeStatusQueryLooper.getWaitQuerySize();
    clientCollect.setWaitQuerySize(waitQuerySize);
    return clientCollect;
  }
}
