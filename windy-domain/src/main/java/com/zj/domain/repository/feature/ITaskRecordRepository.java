package com.zj.domain.repository.feature;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.feature.TaskRecordBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface ITaskRecordRepository {

    boolean save(TaskRecordBO taskRecordBO);

    boolean updateRecordStatus(String recordId, int status);

    PageSize<TaskRecordBO> getTaskRecordPage(Integer pageNum, Integer size);

    TaskRecordBO getTaskRecord(String recordId);

    boolean deleteTaskRecord(String recordId);

    List<TaskRecordBO> getTaskRecordsOrderByTime(String taskId);

    TaskRecordBO getTaskRecordByTrigger(String triggerId);

    PageSize<TaskRecordBO> getTriggerTaskRecords(String triggerId, Integer pageNum, Integer size);
}
