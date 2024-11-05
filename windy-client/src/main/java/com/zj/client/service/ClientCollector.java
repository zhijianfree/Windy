package com.zj.client.service;

import com.zj.common.entity.service.LanguageVersionDto;
import com.zj.client.handler.pipeline.executer.notify.NodeStatusQueryLooper;
import com.zj.common.adapter.monitor.collector.InstanceCollector;
import com.zj.common.adapter.monitor.collector.PhysicsCollect;
import com.zj.common.entity.dto.ClientCollectDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/7/4
 */
@Slf4j
@Service
public class ClientCollector {

  private final NodeStatusQueryLooper nodeStatusQueryLooper;

  public ClientCollector(NodeStatusQueryLooper nodeStatusQueryLooper) {
    this.nodeStatusQueryLooper = nodeStatusQueryLooper;
  }

  public ClientCollectDto getInstanceInfo() {
    ClientCollectDto clientCollectDto = new ClientCollectDto();
    PhysicsCollect physics = InstanceCollector.collectPhysics();
    clientCollectDto.setPhysics(physics);
    Integer waitQuerySize = nodeStatusQueryLooper.getWaitQuerySize();
    clientCollectDto.setWaitQuerySize(waitQuerySize);
    return clientCollectDto;
  }

  public LanguageVersionDto getLanguageVersions() {
    File javaDir = new File("/opt/windy/build/java");
    List<String> javaList = getVersionsFromDir(javaDir);

    File goDir = new File("/opt/windy/build/java");
    List<String> goList = getVersionsFromDir(goDir);
    LanguageVersionDto languageVersionDto = new LanguageVersionDto();
    languageVersionDto.setJavaVersions(javaList);
    languageVersionDto.setGoVersions(goList);
    return languageVersionDto;
  }

  private static List<String> getVersionsFromDir(File dir) {
    Collection<File> javaVersions = FileUtils.listFiles(dir, new String[]{}, false);
      return javaVersions.stream().map(File::getName).collect(Collectors.toList());
  }
}
