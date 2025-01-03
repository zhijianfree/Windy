package com.zj.master.service;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.bo.log.DispatchLogBO;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.master.dispatch.Dispatcher;
import com.zj.master.dispatch.listener.InternalEvent;
import com.zj.master.dispatch.listener.InternalEventFactory;
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

  public Object createTask(DispatchTaskModel task) {
    log.info("receive task detail ={}", JSON.toJSONString(task));
    DispatchLogBO taskLog = saveLog(task);
    return dispatcher.dispatch(task, taskLog.getLogId());
  }

  private DispatchLogBO saveLog(DispatchTaskModel task) {
    DispatchLogBO taskLog = new DispatchLogBO();
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
    InternalEvent event = new InternalEvent();
    event.setEventType(EventType.STOP);
    event.setLogType(LogType.exchange(task.getType()));
    event.setTargetId(task.getSourceId());
    InternalEventFactory.sendNotifyEvent(event);
    return true;
  }
}
