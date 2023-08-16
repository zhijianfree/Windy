package com.zj.client.service;

import com.zj.client.handler.pipeline.executer.notify.NodeStatusQueryLooper;
import com.zj.common.model.ClientCollect;
import com.zj.common.monitor.collector.InstanceCollector;
import com.zj.common.monitor.collector.PhysicsCollect;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

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

  public Boolean uploadTemplate(MultipartFile[] files) {
    try {
      String currentPath =
          new File("").getCanonicalPath() + File.separator + PLUGINS_PATH + File.separator;
      for (MultipartFile file : files) {
        String filePath = currentPath + file.getOriginalFilename();
        createIfNotExist(filePath);
        FileUtils.writeByteArrayToFile(new File(filePath), file.getBytes());
      }
    } catch (Exception e) {
      log.error("save file error", e);
    }
    return false;
  }

  private void createIfNotExist(String filePath) {
    File fileDir = new File(filePath);
    try {
      if (!fileDir.exists()) {
        FileUtils.createParentDirectories(fileDir);
      }
    } catch (IOException ignore) {
    }
  }

}
