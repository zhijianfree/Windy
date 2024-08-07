package com.zj.demand.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.demand.service.WorkTaskService;
import com.zj.domain.entity.dto.demand.WorkTaskDTO;
import com.zj.domain.entity.dto.demand.BusinessStatusDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/devops/work")
public class WorkTaskRest {
    private final WorkTaskService workTaskService;

    public WorkTaskRest(WorkTaskService workTaskService) {
        this.workTaskService = workTaskService;
    }

    @PostMapping("/tasks")
    public ResponseMeta<WorkTaskDTO> createWorkTask(@Validated @RequestBody WorkTaskDTO bugDTO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, workTaskService.createWorkTask(bugDTO));
    }

    @PutMapping("/task")
    public ResponseMeta<Boolean> updateWorkTask(@Validated @RequestBody WorkTaskDTO bugDTO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, workTaskService.updateWorkTask(bugDTO));
    }

    @GetMapping("/tasks")
    public ResponseMeta<PageSize<WorkTaskDTO>> getWorkTaskPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                     @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                     @RequestParam(value = "name", required = false) String name,
                                                     @RequestParam(value = "status", required = false) Integer status) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, workTaskService.getWorkTaskPage(page, size, name, status));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseMeta<WorkTaskDTO> getWorkTask(@PathVariable("taskId") String taskId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, workTaskService.getWorkTask(taskId));
    }

    @GetMapping("/task/statuses")
    public ResponseMeta<List<BusinessStatusDTO>> getWorkTaskStatuses() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, workTaskService.getWorkTaskStatuses());
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseMeta<Boolean> deleteWorkTask(@PathVariable("taskId") String taskId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, workTaskService.deleteWorkTask(taskId));
    }
}
