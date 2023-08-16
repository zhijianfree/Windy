package com.zj.master.schedule;

import com.zj.common.monitor.InstanceMonitor;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.master.dispatch.Dispatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/11
 */
@Slf4j
@Component
public class DispatchLogSchedule {

  public static final String WINDY_MASTER_NAME = "WindyMaster";
  public static final int MAX_REDO_TIMEOUT = 1000 * 60 * 60;

  private final IDispatchLogRepository taskLogRepository;
  private final ISubDispatchLogRepository subDispatchLogRepository;
  private final DiscoveryClient discoveryClient;
  private final InstanceMonitor instanceMonitor;
  private final Dispatcher dispatcher;

  public DispatchLogSchedule(IDispatchLogRepository taskLogRepository,
      ISubDispatchLogRepository subDispatchLogRepository, DiscoveryClient discoveryClient,
      InstanceMonitor instanceMonitor, Dispatcher dispatcher) {
    this.taskLogRepository = taskLogRepository;
    this.subDispatchLogRepository = subDispatchLogRepository;
    this.discoveryClient = discoveryClient;
    this.instanceMonitor = instanceMonitor;
    this.dispatcher = dispatcher;
  }

  @Scheduled(cron = "0 0 0/1 * * ? ")
  public void scanTaskLog() {
    if (instanceMonitor.isUnStable()) {
      return;
    }
    log.debug("start scan log......");
    // 1 扫描任务日志，只获取正在执行中的日志
    List<DispatchLogDto> runningTaskLog = taskLogRepository.getRunningDispatchLog();
    if (CollectionUtils.isEmpty(runningTaskLog)) {
      return;
    }

    // 2 判断扫描到的任务执行的master节点是否还存在，不存在准备进入重选节点流程
    List<DispatchLogDto> needRunList = resolveNoMasterTaskLog(runningTaskLog);

    // 3 如果当前节点重启后由于IP未变化，但是重启节点没有执行任务
    List<DispatchLogDto> localIpNoRun = resolveLocalIpTaskLog(runningTaskLog);
    needRunList.addAll(localIpNoRun);
    List<DispatchLogDto> logs = needRunList.stream().distinct().collect(Collectors.toList());
    log.debug("start run no master task size={}", logs.size());
    // 4 筛选出来的任务开始切换到当前节点执行
    logs.forEach(dispatcher::resumeTask);
  }

  private List<DispatchLogDto> resolveLocalIpTaskLog(List<DispatchLogDto> runningTaskLog) {
    String localIP = IpUtils.getLocalIP();
    return runningTaskLog.stream().filter(log -> Objects.equals(localIP, log.getNodeIp()))
        .filter(log -> (System.currentTimeMillis() - log.getUpdateTime()) > MAX_REDO_TIMEOUT)
        .filter(log -> !dispatcher.isExitInJvm(log)).collect(Collectors.toList());
  }


  @Scheduled(cron = "0 0 0 * * ?")
  public void deleteOldLog() {
    List<String> logIds = taskLogRepository.delete7DayLog();
    if (CollectionUtils.isEmpty(logIds)) {
      return;
    }

    subDispatchLogRepository.batchDeleteByLogIds(logIds);
  }

  private List<DispatchLogDto> resolveNoMasterTaskLog(List<DispatchLogDto> runningTaskLog) {
    String localIP = IpUtils.getLocalIP();
    List<String> masterIps = getCurrentMasterIpList();
    return runningTaskLog.stream().filter(log -> !Objects.equals(localIP, log.getNodeIp()))
        .filter(log -> !masterIps.contains(log.getNodeIp())).filter(
            //使用乐观锁，保证当前任务只被一个节点覆盖
            log -> taskLogRepository.updateLogMasterIp(log.getLogId(), localIP,
                log.getLockVersion())).collect(Collectors.toList());
  }

  private List<String> getCurrentMasterIpList() {
    List<ServiceInstance> instances = discoveryClient.getInstances(WINDY_MASTER_NAME);
    if (CollectionUtils.isEmpty(instances)) {
      return Collections.emptyList();
    }

    return instances.stream().map(ServiceInstance::getHost).collect(Collectors.toList());
  }
}
