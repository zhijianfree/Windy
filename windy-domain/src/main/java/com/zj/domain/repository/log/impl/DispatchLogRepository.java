package com.zj.domain.repository.log.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.domain.entity.po.log.DispatchLog;
import com.zj.domain.mapper.log.TaskLogMapper;
import com.zj.domain.repository.log.IDispatchLogRepository;
import java.util.List;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

/**
 * @author falcon
 * @since 2023/5/19
 */
@Repository
public class DispatchLogRepository extends ServiceImpl<TaskLogMapper, DispatchLog> implements
    IDispatchLogRepository {

  @Override
  public List<DispatchLogDto> getRunningTaskLog() {
    List<DispatchLog> dispatchLogs = list(Wrappers.lambdaQuery(DispatchLog.class)
        .eq(DispatchLog::getLogStatus, ProcessStatus.RUNNING.getType()));
    return OrikaUtil.convertList(dispatchLogs, DispatchLogDto.class);
  }

  @Override
  public void delete7DayLog() {
    long dateTime = new DateTime().minusDays(7).toDate().getTime();
    remove(Wrappers.lambdaQuery(DispatchLog.class).lt(DispatchLog::getCreateTime, dateTime));
  }

  @Override
  public void saveLog(DispatchLogDto dispatchLogDto) {
    DispatchLog dispatchLog = OrikaUtil.convert(dispatchLogDto, DispatchLog.class);
    Long dateNow = System.currentTimeMillis();
    dispatchLog.setCreateTime(dateNow);
    dispatchLog.setUpdateTime(dateNow);
    save(dispatchLog);
  }

  @Override
  public void updateLogStatus(String logId, int status) {
    DispatchLog dispatchLog = new DispatchLog();
    dispatchLog.setLogId(logId);
    dispatchLog.setLogStatus(status);
    dispatchLog.setUpdateTime(System.currentTimeMillis());
    update(dispatchLog, Wrappers.lambdaUpdate(DispatchLog.class).eq(DispatchLog::getLogId, logId));
  }

  @Override
  public boolean updateLogMasterIp(String logId, String localIP, Integer lockVersion) {
    DispatchLog dispatchLog = new DispatchLog();
    dispatchLog.setLogId(logId);
    dispatchLog.setNodeIp(localIP);
    dispatchLog.setUpdateTime(System.currentTimeMillis());
    dispatchLog.setLockVersion(lockVersion);
    return update(dispatchLog,
        Wrappers.lambdaUpdate(DispatchLog.class).eq(DispatchLog::getLogId, logId)
            .eq(DispatchLog::getLockVersion, lockVersion));
  }

  @Override
  public void updateLogSourceRecord(String logId, String sourceRecordId) {
    DispatchLog dispatchLog = new DispatchLog();
    dispatchLog.setLogId(logId);
    dispatchLog.setSourceRecordId(sourceRecordId);
    dispatchLog.setUpdateTime(System.currentTimeMillis());
    update(dispatchLog, Wrappers.lambdaUpdate(DispatchLog.class).eq(DispatchLog::getLogId, logId));
  }
}
