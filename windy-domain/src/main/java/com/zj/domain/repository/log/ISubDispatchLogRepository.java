package com.zj.domain.repository.log;

import com.zj.domain.entity.bo.log.SubDispatchLogBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface ISubDispatchLogRepository {

  void batchSaveLogs(List<SubDispatchLogBO> subTaskLogs);

  void updateLogStatus(String logId, String executeId, Integer status);

  List<SubDispatchLogBO> getSubLogByLogId(String logId);

  void batchDeleteByLogIds(List<String> logIds);

  SubDispatchLogBO getSubDispatchLog(String logId, String executeId);

  void updateSubLogClientIp(String logId, String nodeId, String clientIp);
}
