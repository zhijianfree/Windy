package com.zj.domain.repository.log.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.log.DispatchLogBO;
import com.zj.domain.entity.po.log.DispatchLog;
import com.zj.domain.mapper.log.DispatchLogMapper;
import com.zj.domain.repository.log.IDispatchLogRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
@Repository
public class DispatchLogRepository extends ServiceImpl<DispatchLogMapper, DispatchLog> implements
    IDispatchLogRepository {

  public static final int PLUS_VERSION = 1;

  @Override
  public List<DispatchLogBO> getRunningDispatchLog() {
    List<DispatchLog> dispatchLogs = list(Wrappers.lambdaQuery(DispatchLog.class)
        .eq(DispatchLog::getLogStatus, ProcessStatus.RUNNING.getType()));
    return OrikaUtil.convertList(dispatchLogs, DispatchLogBO.class);
  }

  @Override
  public List<String> delete7DayLog() {
    long dateTime = new DateTime().minusDays(7).toDate().getTime();
    List<DispatchLog> dispatchLogs = list(
        Wrappers.lambdaQuery(DispatchLog.class).lt(DispatchLog::getCreateTime, dateTime));
    if (CollectionUtils.isEmpty(dispatchLogs)) {
      return Collections.emptyList();
    }

    boolean result = remove(
        Wrappers.lambdaQuery(DispatchLog.class).lt(DispatchLog::getCreateTime, dateTime));
    return result ? dispatchLogs.stream().map(DispatchLog::getLogId).collect(Collectors.toList())
        : Collections.emptyList();
  }

  @Override
  public void saveLog(DispatchLogBO dispatchLogBO) {
    DispatchLog dispatchLog = OrikaUtil.convert(dispatchLogBO, DispatchLog.class);
    Long dateNow = System.currentTimeMillis();
    dispatchLog.setCreateTime(dateNow);
    dispatchLog.setUpdateTime(dateNow);
    save(dispatchLog);
  }

  @Override
  public boolean updateLogStatus(String logId, int status) {
    DispatchLog dispatchLog = new DispatchLog();
    dispatchLog.setLogId(logId);
    dispatchLog.setLogStatus(status);
    dispatchLog.setUpdateTime(System.currentTimeMillis());
    return update(dispatchLog, Wrappers.lambdaUpdate(DispatchLog.class).eq(DispatchLog::getLogId, logId));
  }

  @Override
  public boolean updateLogMasterIp(String logId, String localIP, Integer lockVersion) {
    DispatchLog dispatchLog = new DispatchLog();
    dispatchLog.setLogId(logId);
    dispatchLog.setNodeIp(localIP);
    dispatchLog.setUpdateTime(System.currentTimeMillis());
    dispatchLog.setLockVersion(lockVersion + PLUS_VERSION);
    return update(dispatchLog,
        Wrappers.lambdaUpdate(DispatchLog.class).eq(DispatchLog::getLogId, logId)
            .eq(DispatchLog::getLockVersion, lockVersion));
  }

  @Override
  public boolean updateLogSourceRecord(String logId, String sourceRecordId) {
    DispatchLog dispatchLog = new DispatchLog();
    dispatchLog.setLogId(logId);
    dispatchLog.setSourceRecordId(sourceRecordId);
    dispatchLog.setUpdateTime(System.currentTimeMillis());
    return update(dispatchLog, Wrappers.lambdaUpdate(DispatchLog.class).eq(DispatchLog::getLogId, logId));
  }

  @Override
  public DispatchLogBO getDispatchLog(String logId) {
    DispatchLog dispatchLog = getOne(
        Wrappers.lambdaQuery(DispatchLog.class).eq(DispatchLog::getLogId, logId));
    return OrikaUtil.convert(dispatchLog, DispatchLogBO.class);
  }
}
