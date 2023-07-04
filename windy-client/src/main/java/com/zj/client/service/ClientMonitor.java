package com.zj.client.service;

import com.zj.common.model.ClientCollect;
import com.zj.client.handler.pipeline.executer.notify.NodeStatusQueryLooper;
import com.zj.common.monitor.collector.InstanceCollector;
import com.zj.common.monitor.collector.PhysicsCollect;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Service
public class ClientMonitor {

  private NodeStatusQueryLooper nodeStatusQueryLooper;

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
