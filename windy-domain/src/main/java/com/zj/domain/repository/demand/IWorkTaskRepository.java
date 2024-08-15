package com.zj.domain.repository.demand;

import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.demand.TaskQuery;
import com.zj.domain.entity.dto.demand.WorkTaskDTO;

import java.util.List;

public interface IWorkTaskRepository {
    boolean createTask(WorkTaskDTO workTask);

    boolean updateWorkTask(WorkTaskDTO workTask);

    WorkTaskDTO getWorkTask(String taskId);

    boolean deleteWorkTask(String taskId);

    PageSize<WorkTaskDTO> getWorkTaskPage(TaskQuery taskQuery);

    Integer countIteration(String iterationId);

    List<WorkTaskDTO> getWorkTaskByName(String queryName);
}
