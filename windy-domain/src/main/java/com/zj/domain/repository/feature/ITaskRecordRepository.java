package com.zj.domain.repository.feature;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.feature.TaskRecordBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface ITaskRecordRepository {

    /**
     * 保存任务记录
     * @param taskRecordBO 任务记录
     * @return 是否成功
     */
    boolean save(TaskRecordBO taskRecordBO);

    /**
     * 更新任务记录状态
     * @param recordId 记录ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateRecordStatus(String recordId, int status);

    /**
     * 分页获取任务记录列表
     * @param pageNum 页码
     * @param size 每页数量
     * @return 任务记录列表
     */
    PageSize<TaskRecordBO> getTaskRecordPage(Integer pageNum, Integer size);

    /**
     * 获取任务记录
     * @param recordId 记录ID
     * @return 任务记录
     */
    TaskRecordBO getTaskRecord(String recordId);

    /**
     * 删除任务记录
     * @param recordId 记录ID
     * @return 是否成功
     */
    boolean deleteTaskRecord(String recordId);

    /**
     * 获取任务记录列表
     * @param taskId 任务ID
     * @return 任务记录列表
     */
    List<TaskRecordBO> getTaskRecords(String taskId);

    /**
     * 根据任务触发源ID获取任务记录
     * @param triggerId 触发器ID
     * @return 任务记录
     */
    TaskRecordBO getTaskRecordByTrigger(String triggerId);

    /**
     * 分页获取触发任务yuanID记录
     * @param triggerId 触发器ID
     * @param pageNum 页码
     * @param size 每页数量
     * @return 任务记录列表
     */
    PageSize<TaskRecordBO> getTriggerTaskRecords(String triggerId, Integer pageNum, Integer size);

    /**
     * 获取旧的任务记录
     * @param oldTime 时间
     * @return 任务记录列表
     */
    List<TaskRecordBO> getOldTaskRecord(long oldTime);
}
