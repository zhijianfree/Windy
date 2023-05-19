package com.zj.domain.repository.log;

import com.zj.domain.entity.dto.log.SubTaskLogDto;
import java.util.List;

/**
 * @author falcon
 * @since 2023/5/19
 */
public interface ISubTaskLogRepository {

  void batchSaveLogs(List<SubTaskLogDto> subTaskLogs);

  void updateLogStatus(String logId, String executeId, Integer status);

  List<SubTaskLogDto> getSubTaskByLogId(String logId);
}
