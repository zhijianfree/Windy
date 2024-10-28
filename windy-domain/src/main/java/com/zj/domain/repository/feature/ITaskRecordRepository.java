package com.zj.domain.repository.feature;

import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.feature.TaskRecordDto;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface ITaskRecordRepository {

    boolean save(TaskRecordDto taskRecordDTO);

    boolean updateRecordStatus(String recordId, int status);

    PageSize<TaskRecordDto> getTaskRecordPage(Integer pageNum, Integer size);

    TaskRecordDto getTaskRecord(String recordId);

    boolean deleteTaskRecord(String recordId);

    List<TaskRecordDto> getTaskRecordsOrderByTime(String taskId);

    TaskRecordDto getTaskRecordByTrigger(String triggerId);

    PageSize<TaskRecordDto> getTriggerTaskRecords(String triggerId, Integer pageNum, Integer size);
}
