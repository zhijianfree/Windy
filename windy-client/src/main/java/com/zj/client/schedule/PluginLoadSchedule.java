package com.zj.client.schedule;

import com.zj.client.handler.feature.executor.invoker.invoke.MethodInvoke;
import com.zj.client.handler.feature.executor.invoker.loader.PluginManager;
import com.zj.common.entity.dto.PluginInfo;
import com.zj.common.adapter.monitor.InstanceMonitor;
import com.zj.common.adapter.invoker.IMasterInvoker;
import com.zj.common.adapter.trace.TidInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/7/17
 */
@Slf4j
@Component
public class PluginLoadSchedule {

  private final IMasterInvoker masterInvoker;
  private final PluginManager pluginManager;
  private final InstanceMonitor instanceMonitor;
  private final MethodInvoke methodInvoke;

  public PluginLoadSchedule(IMasterInvoker masterInvoker, PluginManager pluginManager,
      InstanceMonitor instanceMonitor, MethodInvoke methodInvoke) {
    this.masterInvoker = masterInvoker;
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
    List<PluginInfo> plugins = masterInvoker.getAvailablePlugins();
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
