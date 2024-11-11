package com.zj.domain.repository.log;

import com.zj.domain.entity.bo.log.SubDispatchLogDto;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface ISubDispatchLogRepository {

  void batchSaveLogs(List<SubDispatchLogDto> subTaskLogs);

  void updateLogStatus(String logId, String executeId, Integer status);

  List<SubDispatchLogDto> getSubLogByLogId(String logId);

  void batchDeleteByLogIds(List<String> logIds);

  SubDispatchLogDto getSubDispatchLog(String logId, String executeId);

  void updateSubLogClientIp(String logId, String nodeId, String clientIp);
}
