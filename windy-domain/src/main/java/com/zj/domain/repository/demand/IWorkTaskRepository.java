package com.zj.domain.repository.demand;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.demand.TaskQueryBO;
import com.zj.domain.entity.bo.demand.WorkTaskBO;

import java.util.List;

public interface IWorkTaskRepository {
    boolean createTask(WorkTaskBO workTask);

    boolean updateWorkTask(WorkTaskBO workTask);

    WorkTaskBO getWorkTask(String taskId);

    boolean deleteWorkTask(String taskId);

    PageSize<WorkTaskBO> getWorkTaskPage(TaskQueryBO taskQueryBO);

    Integer countIteration(String iterationId);

    List<WorkTaskBO> getWorkTaskByName(String queryName);
}
