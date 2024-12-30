package com.zj.domain.repository.demand;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.demand.TaskQueryBO;
import com.zj.domain.entity.bo.demand.WorkTaskBO;

import java.util.List;

public interface IWorkTaskRepository {

    /**
     * 创建任务
     * @param workTask 任务信息
     * @return 是否成功
     */
    boolean createTask(WorkTaskBO workTask);

    /**
     * 更新任务
     * @param workTask 任务信息
     * @return 是否成功
     */
    boolean updateWorkTask(WorkTaskBO workTask);

    /**
     * 获取任务详情
     * @param taskId 任务ID
     * @return 任务信息
     */
    WorkTaskBO getWorkTask(String taskId);

    /**
     * 删除任务
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean deleteWorkTask(String taskId);

    /**
     * 分页获取任务列表
     * @param taskQueryBO 查询条件
     * @return 任务列表
     */
    PageSize<WorkTaskBO> getWorkTaskPage(TaskQueryBO taskQueryBO);

    /**
     * 模糊查询任务列表
     * @param queryName 查询名称
     * @return 任务列表
     */
    List<WorkTaskBO> getWorkTaskByName(String queryName);

    /**
     * 获取未完成任务列表
     * @param taskIds 任务ID列表
     * @return 任务列表
     */
    List<WorkTaskBO> getNotCompleteWorkTasks(List<String> taskIds);

    /**
     * 批量更新任务状态
     * @param notCompleteTaskIds 未完成任务ID列表
     * @param status 状态
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<String> notCompleteTaskIds, int status);
}
