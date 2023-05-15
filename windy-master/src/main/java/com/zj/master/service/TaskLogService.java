package com.zj.master.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.IpUtils;
import com.zj.master.dispatch.Dispatcher;
import com.zj.master.entity.dto.TaskDetailDto;
import com.zj.domain.entity.po.log.TaskLog;
import com.zj.domain.mapper.log.TaskLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author falcon
 * @since 2023/5/12
 */
@Slf4j
@Service
public class TaskLogService extends ServiceImpl<TaskLogMapper, TaskLog> {

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private Dispatcher dispatcher;

  public Boolean createTask(TaskDetailDto task) {
    log.info("receive task detail ={}", JSON.toJSONString(task));
    saveLog(task);
    return dispatcher.dispatch(task);
  }

  private void saveLog(TaskDetailDto task) {
    TaskLog taskLog = new TaskLog();
    taskLog.setLogId(uniqueIdService.getUniqueId());
    taskLog.setSourceId(task.getSourceId());
    taskLog.setSourceName(task.getSourceName());
    taskLog.setLogStatus(ProcessStatus.RUNNING.getType());
    Long dateNow = System.currentTimeMillis();
    taskLog.setCreateTime(dateNow);
    taskLog.setUpdateTime(dateNow);
    taskLog.setLogType(taskLog.getLogType());
    taskLog.setNodeIp(IpUtils.getLocalIP());
    save(taskLog);
  }
}
