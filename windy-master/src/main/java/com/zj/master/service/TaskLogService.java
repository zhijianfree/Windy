package com.zj.master.service;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.DispatchTaskModel;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.master.dispatch.Dispatcher;
import com.zj.master.dispatch.listener.InnerEvent;
import com.zj.master.dispatch.listener.TaskInnerEventFactory;
import com.zj.master.entity.enums.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/5/12
 */
@Slf4j
@Service
public class TaskLogService {

  private final UniqueIdService uniqueIdService;
  private final IDispatchLogRepository taskLogRepository;
  private final Dispatcher dispatcher;

  public TaskLogService(UniqueIdService uniqueIdService, IDispatchLogRepository taskLogRepository,
      Dispatcher dispatcher) {
    this.uniqueIdService = uniqueIdService;
    this.taskLogRepository = taskLogRepository;
    this.dispatcher = dispatcher;
  }

  public String createTask(DispatchTaskModel task) {
    log.info("receive task detail ={}", JSON.toJSONString(task));
    DispatchLogDto taskLog = saveLog(task);
    return dispatcher.dispatch(task, taskLog.getLogId());
  }

  private DispatchLogDto saveLog(DispatchTaskModel task) {
    DispatchLogDto taskLog = new DispatchLogDto();
    taskLog.setLogId(uniqueIdService.getUniqueId());
    taskLog.setSourceId(task.getSourceId());
    taskLog.setSourceName(task.getSourceName());
    taskLog.setLogStatus(ProcessStatus.RUNNING.getType());
    taskLog.setLogType(task.getType());
    taskLog.setNodeIp(IpUtils.getLocalIP());
    taskLog.setLockVersion(1);
    taskLogRepository.saveLog(taskLog);
    return taskLog;
  }

  public Boolean pauseTask(DispatchTaskModel task) {
    InnerEvent event = new InnerEvent();
    event.setEventType(EventType.STOP);
    event.setLogType(LogType.exchange(task.getType()));
    event.setTargetId(task.getSourceId());
    TaskInnerEventFactory.sendNotifyEvent(event);
    return true;
  }
}
