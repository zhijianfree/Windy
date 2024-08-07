package com.zj.demand.service;

import com.zj.common.model.PageSize;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.demand.BusinessStatusDTO;
import com.zj.domain.entity.dto.demand.TaskQuery;
import com.zj.domain.entity.dto.demand.WorkTaskDTO;
import com.zj.domain.repository.demand.IBusinessStatusRepository;
import com.zj.domain.repository.demand.IWorkTaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkTaskService {

    private final UniqueIdService uniqueIdService;
    private final IWorkTaskRepository workTaskRepository;
    private final IBusinessStatusRepository businessStatusRepository;
    private final IAuthService authService;

    public WorkTaskService(UniqueIdService uniqueIdService, IWorkTaskRepository workTaskRepository, IBusinessStatusRepository businessStatusRepository, IAuthService authService) {
        this.uniqueIdService = uniqueIdService;
        this.workTaskRepository = workTaskRepository;
        this.businessStatusRepository = businessStatusRepository;
        this.authService = authService;
    }

    public WorkTaskDTO createWorkTask(WorkTaskDTO workTask) {
        workTask.setTaskId(uniqueIdService.getUniqueId());
        workTask.setCreator(authService.getCurrentUserId());
        boolean result = workTaskRepository.createTask(workTask);
        return result ? workTask : null;
    }

    public Boolean updateWorkTask(WorkTaskDTO workTask) {
        return workTaskRepository.updateWorkTask(workTask);
    }

    public PageSize<WorkTaskDTO> getWorkTaskPage(Integer page, Integer size, String name, Integer status) {
        String userId = authService.getCurrentUserId();
        TaskQuery taskQuery = TaskQuery.builder().page(page).size(size).name(name).userId(userId).status(status).build();
        return workTaskRepository.getWorkTaskPage(taskQuery);
    }

    public WorkTaskDTO getWorkTask(String taskId) {
        return workTaskRepository.getWorkTask(taskId);
    }

    public List<BusinessStatusDTO> getWorkTaskStatuses() {
        return businessStatusRepository.getWorkTaskStatuses();
    }

    public Boolean deleteWorkTask(String taskId) {
        return workTaskRepository.deleteWorkTask(taskId);
    }
}
