package com.zj.client.schedule;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.feature.executor.invoker.invoke.MethodInvoke;
import com.zj.client.handler.feature.executor.invoker.loader.PluginManager;
import com.zj.common.model.PluginInfo;
import com.zj.common.monitor.InstanceMonitor;
import com.zj.common.monitor.RequestProxy;
import com.zj.common.monitor.trace.TidInterceptor;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/7/17
 */
@Slf4j
@Component
public class PluginLoadSchedule {

  private final RequestProxy requestProxy;
  private final PluginManager pluginManager;
  private final InstanceMonitor instanceMonitor;
  private final MethodInvoke methodInvoke;

  public PluginLoadSchedule(RequestProxy requestProxy, PluginManager pluginManager,
      InstanceMonitor instanceMonitor, MethodInvoke methodInvoke) {
    this.requestProxy = requestProxy;
    this.pluginManager = pluginManager;
    this.instanceMonitor = instanceMonitor;
    this.methodInvoke = methodInvoke;
  }

  @Scheduled(cron = "0/5 * * * * ? ")
  public void loadSchedule() {
    if (instanceMonitor.isUnStable()) {
      return;
    }

    initMDC();
    List<PluginInfo> plugins = requestProxy.getAvailablePlugins();
    if (CollectionUtils.isEmpty(plugins)) {
      return;
    }

    List<String> localNames = loadLocalJarFiles();
    List<PluginInfo> needLoad = plugins.stream()
        .filter(plugin -> !localNames.contains(plugin.getPluginName()))
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(needLoad)) {
      return;
    }

    log.info("find plugin to load={}", needLoad.stream().map(PluginInfo::getPluginName).collect(
        Collectors.toList()));
    needLoad.forEach(pluginManager::saveJarPlugin);
    methodInvoke.loadPluginFromDisk();
  }

  private static void initMDC() {
    MDC.put(TidInterceptor.MDC_TID_KEY, UUID.randomUUID().toString().replace("-",""));
  }

  private List<String> loadLocalJarFiles() {
    try {
      URL[] fileUrls = pluginManager.getFileUrls();
      return Arrays.stream(fileUrls).map(url -> {
        String filePath = url.getFile();
        String[] strings = filePath.split("/");
        return strings[strings.length - 1];
      }).collect(Collectors.toList());
    } catch (Exception e) {
      log.error("load file from local error", e);
    }
    return Collections.emptyList();
  }
}
