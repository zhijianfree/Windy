package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import java.util.List;

/**
 * @author falcon
 * @since 2023/5/17
 */
public interface ITaskRecordRepository {

  boolean save(TaskRecordDto taskRecordDTO);

  boolean updateRecordStatus(String recordId, int status);

  IPage<TaskRecordDto> getTaskRecordPage(Integer pageNum, Integer size);

  TaskRecordDto getTaskRecord(String recordId);

  boolean deleteTaskRecord(String recordId);

  List<TaskRecordDto> getTaskRecordsOrderByTime(String taskId);
}
