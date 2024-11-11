package com.zj.demand.service;

import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.demand.entity.WorkTaskDto;
import com.zj.domain.entity.bo.demand.BusinessStatusBO;
import com.zj.domain.entity.bo.demand.TaskQueryBO;
import com.zj.domain.entity.bo.demand.WorkTaskBO;
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

    public WorkTaskBO createWorkTask(WorkTaskDto workTaskDto) {
        WorkTaskBO workTaskBO = OrikaUtil.convert(workTaskDto, WorkTaskBO.class);
        workTaskBO.setTaskId(uniqueIdService.getUniqueId());
        workTaskBO.setCreator(authService.getCurrentUserId());
        boolean result = workTaskRepository.createTask(workTaskBO);
        return result ? workTaskBO : null;
    }

    public Boolean updateWorkTask(WorkTaskDto workTaskDto) {
        return workTaskRepository.updateWorkTask(OrikaUtil.convert(workTaskDto, WorkTaskBO.class));
    }

    public PageSize<WorkTaskBO> getWorkTaskPage(Integer page, Integer size, String name, Integer status) {
        String userId = authService.getCurrentUserId();
        TaskQueryBO taskQueryBO = TaskQueryBO.builder().page(page).size(size).name(name).userId(userId).status(status).build();
        return workTaskRepository.getWorkTaskPage(taskQueryBO);
    }

    public WorkTaskBO getWorkTask(String taskId) {
        return workTaskRepository.getWorkTask(taskId);
    }

    public List<BusinessStatusBO> getWorkTaskStatuses() {
        return businessStatusRepository.getWorkTaskStatuses();
    }

    public Boolean deleteWorkTask(String taskId) {
        return workTaskRepository.deleteWorkTask(taskId);
    }
}
