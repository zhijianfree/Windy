package com.zj.domain.repository.log;

import com.zj.domain.entity.dto.log.TaskLogDto;
import com.zj.domain.entity.po.log.TaskLog;
import java.util.List;

/**
 * @author falcon
 * @since 2023/5/19
 */
public interface ITaskLogRepository {

  List<TaskLogDto> getRunningTaskLog();

  void delete7DayLog();

  void saveLog(TaskLogDto taskLog);

  void updateLogStatus(String logId, int type);

  boolean updateLogMasterIp(String logId, String localIP, Integer lockVersion);
}
