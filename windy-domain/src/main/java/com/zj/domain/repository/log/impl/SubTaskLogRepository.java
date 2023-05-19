package com.zj.domain.repository.log.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.log.SubTaskLogDto;
import com.zj.domain.entity.po.log.SubTaskLog;
import com.zj.domain.mapper.log.SubTaskLogMapper;
import com.zj.domain.repository.log.ISubTaskLogRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author falcon
 * @since 2023/5/19
 */
@Repository
public class SubTaskLogRepository extends ServiceImpl<SubTaskLogMapper, SubTaskLog> implements
    ISubTaskLogRepository {

  @Override
  public void batchSaveLogs(List<SubTaskLogDto> subTaskLogs) {
    List<SubTaskLog> subLogs = OrikaUtil.convertList(subTaskLogs, SubTaskLog.class);
    saveBatch(subLogs);
  }

  @Override
  public void updateLogStatus(String logId, String executeId, Integer status) {
    SubTaskLog subTaskLog = new SubTaskLog();
    subTaskLog.setLogId(logId);
    subTaskLog.setExecuteId(executeId);
    subTaskLog.setStatus(status);
    update(subTaskLog, Wrappers.lambdaUpdate(SubTaskLog.class).eq(SubTaskLog::getLogId, logId)
        .eq(SubTaskLog::getExecuteId, executeId));
  }

  @Override
  public List<SubTaskLogDto> getSubTaskByLogId(String logId) {
    List<SubTaskLog> taskLogs = list(
        Wrappers.lambdaQuery(SubTaskLog.class).eq(SubTaskLog::getLogId, logId));
    return OrikaUtil.convertList(taskLogs, SubTaskLogDto.class);
  }
}
