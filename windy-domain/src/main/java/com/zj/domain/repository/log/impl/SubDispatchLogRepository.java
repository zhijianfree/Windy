package com.zj.domain.repository.log.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.log.SubDispatchLogBO;
import com.zj.domain.entity.po.log.SubDispatchLog;
import com.zj.domain.mapper.log.SubTaskLogMapper;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
@Repository
public class SubDispatchLogRepository extends
    ServiceImpl<SubTaskLogMapper, SubDispatchLog> implements ISubDispatchLogRepository {

  @Override
  public void batchSaveLogs(List<SubDispatchLogBO> subTaskLogs) {
    List<SubDispatchLog> subLogs = OrikaUtil.convertList(subTaskLogs, SubDispatchLog.class);
    saveBatch(subLogs);
  }

  @Override
  public void updateLogStatus(String logId, String executeId, Integer status) {
    SubDispatchLogBO existLog = getSubDispatchLog(logId, executeId);
    if (Objects.isNull(existLog)) {
      return;
    }

    SubDispatchLog subDispatchLog = new SubDispatchLog();
    subDispatchLog.setLogId(logId);
    subDispatchLog.setExecuteId(executeId);
    subDispatchLog.setStatus(status);
    update(subDispatchLog,
        Wrappers.lambdaUpdate(SubDispatchLog.class).eq(SubDispatchLog::getLogId, logId)
            .eq(SubDispatchLog::getExecuteId, executeId));
  }

  @Override
  public List<SubDispatchLogBO> getSubLogByLogId(String logId) {
    List<SubDispatchLog> taskLogs = list(
        Wrappers.lambdaQuery(SubDispatchLog.class).eq(SubDispatchLog::getLogId, logId));
    return OrikaUtil.convertList(taskLogs, SubDispatchLogBO.class);
  }

  @Override
  public boolean batchDeleteByLogIds(List<String> logIds) {
    return remove(Wrappers.lambdaQuery(SubDispatchLog.class).in(SubDispatchLog::getLogId, logIds));
  }

  @Override
  public SubDispatchLogBO getSubDispatchLog(String logId, String executeId) {
    SubDispatchLog subDispatchLog = getOne(
        Wrappers.lambdaQuery(SubDispatchLog.class).eq(SubDispatchLog::getLogId, logId)
            .eq(SubDispatchLog::getExecuteId, executeId));
    return OrikaUtil.convert(subDispatchLog, SubDispatchLogBO.class);
  }

  @Override
  public void updateSubLogClientIp(String logId, String executeId, String clientIp) {
    SubDispatchLog subDispatchLog = new SubDispatchLog();
    subDispatchLog.setLogId(logId);
    subDispatchLog.setExecuteId(executeId);
    subDispatchLog.setClientIp(clientIp);

    update(subDispatchLog,
        Wrappers.lambdaQuery(SubDispatchLog.class).eq(SubDispatchLog::getLogId, logId)
            .eq(SubDispatchLog::getExecuteId, executeId));
  }
}
