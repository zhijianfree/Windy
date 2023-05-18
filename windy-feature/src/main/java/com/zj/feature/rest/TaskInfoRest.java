package com.zj.feature.rest;

import com.zj.common.model.ResponseMeta;
import com.zj.common.model.ResponseStatusModel;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.feature.TaskInfoDto;
import com.zj.feature.service.TaskInfoService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2022/12/29
 */
@RestController
@RequestMapping("/v1/devops/feature")
public class TaskInfoRest {

  @Autowired
  private TaskInfoService taskInfoService;

  @GetMapping("/tasks")
  public ResponseMeta<PageSize<TaskInfoDto>> getTaskList(@RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size, @RequestParam(value = "name") String name) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskInfoService.getTaskList(name, page, size));
  }

  @GetMapping("/{serviceId}/tasks")
  public ResponseMeta<List<TaskInfoDto>> getAllTaskList(@PathVariable(value = "serviceId") String serviceId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskInfoService.getAllTaskList(serviceId));
  }

  @PostMapping("/task")
  public ResponseMeta<Boolean> createTask(@RequestBody TaskInfoDto taskInfoDTO) {
    Boolean result = taskInfoService.createTask(taskInfoDTO);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @PutMapping("/task")
  public ResponseMeta<Boolean> updateTask(@RequestBody TaskInfoDto taskInfoDTO) {
    Boolean result = taskInfoService.updateTask(taskInfoDTO);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @DeleteMapping("/task/{taskId}")
  public ResponseMeta<Boolean> deleteTask(@PathVariable("taskId") String taskId) {
    Boolean result = taskInfoService.deleteTask(taskId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @GetMapping("/task/{taskId}")
  public ResponseMeta<TaskInfoDto> getTaskDetail(@PathVariable("taskId") String taskId) {
    TaskInfoDto taskDetail = taskInfoService.getTaskDetail(taskId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDetail);
  }

  @PostMapping("/task/{taskId}")
  public ResponseMeta<Boolean> startTask(@PathVariable("taskId") String taskId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskInfoService.startTask(taskId));
  }

  @GetMapping("/task/{taskId}/status")
  public ResponseStatusModel getTaskStatus(@PathVariable("taskId") String taskId) {
    return taskInfoService.getTaskStatus(taskId);
  }
}
