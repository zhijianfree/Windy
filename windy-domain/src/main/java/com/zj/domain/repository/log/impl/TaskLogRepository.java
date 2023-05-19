package com.zj.domain.repository.log.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.log.TaskLogDto;
import com.zj.domain.entity.po.log.TaskLog;
import com.zj.domain.mapper.log.TaskLogMapper;
import com.zj.domain.repository.log.ITaskLogRepository;
import java.util.List;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

/**
 * @author falcon
 * @since 2023/5/19
 */
@Repository
public class TaskLogRepository extends ServiceImpl<TaskLogMapper, TaskLog> implements
    ITaskLogRepository {

  @Override
  public List<TaskLogDto> getRunningTaskLog() {
    List<TaskLog> taskLogs = list(Wrappers.lambdaQuery(TaskLog.class)
        .eq(TaskLog::getLogStatus, ProcessStatus.RUNNING.getType()));
    return OrikaUtil.convertList(taskLogs, TaskLogDto.class);
  }

  @Override
  public void delete7DayLog() {
    long dateTime = new DateTime().minusDays(7).toDate().getTime();
    remove(Wrappers.lambdaQuery(TaskLog.class).lt(TaskLog::getCreateTime, dateTime));
  }

  @Override
  public void saveLog(TaskLogDto taskLogDto) {
    TaskLog taskLog = OrikaUtil.convert(taskLogDto, TaskLog.class);
    Long dateNow = System.currentTimeMillis();
    taskLog.setCreateTime(dateNow);
    taskLog.setUpdateTime(dateNow);
    save(taskLog);
  }

  @Override
  public void updateLogStatus(String logId, int status) {
    TaskLog taskLog = new TaskLog();
    taskLog.setLogId(logId);
    taskLog.setLogStatus(status);
    taskLog.setUpdateTime(System.currentTimeMillis());
    update(taskLog, Wrappers.lambdaUpdate(TaskLog.class).eq(TaskLog::getLogId, logId));
  }

  @Override
  public boolean updateLogMasterIp(String logId, String localIP, Integer lockVersion) {
    TaskLog taskLog = new TaskLog();
    taskLog.setLogId(logId);
    taskLog.setNodeIp(localIP);
    taskLog.setUpdateTime(System.currentTimeMillis());
    taskLog.setLockVersion(lockVersion);
    return update(taskLog, Wrappers.lambdaUpdate(TaskLog.class).eq(TaskLog::getLogId, logId)
        .eq(TaskLog::getLockVersion, lockVersion));
  }
}
