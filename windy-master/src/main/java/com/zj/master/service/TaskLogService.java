package com.zj.master.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.dto.log.TaskLogDto;
import com.zj.domain.repository.log.ITaskLogRepository;
import com.zj.master.dispatch.Dispatcher;
import com.zj.master.dispatch.listener.InnerEvent;
import com.zj.master.dispatch.listener.TaskInnerEventFactory;
import com.zj.master.entity.dto.TaskDetailDto;
import com.zj.domain.entity.po.log.TaskLog;
import com.zj.domain.mapper.log.TaskLogMapper;
import com.zj.master.entity.enums.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/5/12
 */
@Slf4j
@Service
public class TaskLogService {

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private ITaskLogRepository taskLogRepository;

  @Autowired
  private Dispatcher dispatcher;

  public Boolean createTask(TaskDetailDto task) {
    log.info("receive task detail ={}", JSON.toJSONString(task));
    TaskLogDto taskLog = saveLog(task);
    task.setTaskLogId(taskLog.getLogId());
    return dispatcher.dispatch(task);
  }

  private TaskLogDto saveLog(TaskDetailDto task) {
    TaskLogDto taskLog = new TaskLogDto();
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

  public Boolean pauseTask(TaskDetailDto taskDetailDto) {
    InnerEvent event = new InnerEvent();
    event.setEventType(EventType.STOP);
    event.setLogType(LogType.exchange(taskDetailDto.getType()));
    event.setTargetId(taskDetailDto.getSourceId());
    TaskInnerEventFactory.sendNotifyEvent(event);
    return true;
  }
}
