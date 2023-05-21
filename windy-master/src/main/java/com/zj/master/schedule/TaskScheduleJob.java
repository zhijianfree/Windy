package com.zj.master.schedule;

import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.dto.log.SubTaskLogDto;
import com.zj.domain.entity.dto.log.TaskLogDto;
import com.zj.domain.repository.log.ISubTaskLogRepository;
import com.zj.domain.repository.log.ITaskLogRepository;
import com.zj.master.dispatch.Dispatcher;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/11
 */
@Component
public class TaskScheduleJob {

  public static final String WINDY_MASTER_NAME = "WindyMaster";
  @Autowired
  private ITaskLogRepository taskLogRepository;

  @Autowired
  private ISubTaskLogRepository subTaskLogRepository;

  @Autowired
  private DiscoveryClient discoveryClient;

  @Value("${task.log.max.wait.time:300}")
  private Integer maxExecuteWaitTime;

  @Autowired
  private Dispatcher dispatcher;

  @Scheduled(cron = "0 0 0/1 * * ? ")
  public void scanTaskLog() {
    // 1 扫描任务日志，只获取正在执行中的日志
    List<TaskLogDto> runningTaskLog = taskLogRepository.getRunningTaskLog();
    if (CollectionUtils.isEmpty(runningTaskLog)) {
      return;
    }

    // 2 判断扫描到的任务执行的master节点是否还存在，不存在准备进入重选节点流程
    List<TaskLogDto> noMasterLogList = resolveNoMasterTaskLog(runningTaskLog);

    // 3 根据时间判断，在一定的时间间隔内仍未完成的任务日志，转移当前任务的执行节点为自己。
    //todo 暂时不考虑

    // 4 筛选出来的任务开始切换到当前节点执行
    runningTaskLog.forEach(taskLog -> dispatcher.resumeTask(taskLog));
  }


  @Scheduled(cron = "0 0 0 * * ?")
  public void deleteOldLog() {
    taskLogRepository.delete7DayLog();
  }

  private List<TaskLogDto> resolveNoMasterTaskLog(List<TaskLogDto> runningTaskLog) {
    List<String> masterIps = getCurrentMasterIpList();
    return runningTaskLog.stream().filter(log -> masterIps.contains(log.getNodeIp())).filter(
        //使用乐观锁，保证当前任务只被一个节点覆盖
        log -> taskLogRepository.updateLogMasterIp(log.getLogId(), IpUtils.getLocalIP(),
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
