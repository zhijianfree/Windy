package com.zj.feature.rest;

import com.zj.common.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.dto.PageSize;
import com.zj.feature.entity.dto.TaskInfoDTO;
import com.zj.feature.service.TaskInfoService;
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
 * @author falcon
 * @since 2022/12/29
 */
@RestController
@RequestMapping("/v1/devops/feature")
public class TaskInfoRest {

  @Autowired
  private TaskInfoService taskInfoService;

  @GetMapping("/tasks")
  public ResponseMeta<PageSize<TaskInfoDTO>> getTaskList(@RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size, @RequestParam(value = "name") String name) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskInfoService.getTaskList(name, page, size));
  }

  @PostMapping("/task")
  public ResponseMeta<Boolean> createTask(@RequestBody TaskInfoDTO taskInfoDTO) {
    Boolean result = taskInfoService.createTask(taskInfoDTO);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @PutMapping("/task")
  public ResponseMeta<Boolean> updateTask(@RequestBody TaskInfoDTO taskInfoDTO) {
    Boolean result = taskInfoService.updateTask(taskInfoDTO);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @DeleteMapping("/task/{taskId}")
  public ResponseMeta<Boolean> deleteTask(@PathVariable("taskId") String taskId) {
    Boolean result = taskInfoService.deleteTask(taskId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, result);
  }

  @GetMapping("/task/{taskId}")
  public ResponseMeta<TaskInfoDTO> getTaskDetail(@PathVariable("taskId") String taskId) {
    TaskInfoDTO taskDetail = taskInfoService.getTaskDetail(taskId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDetail);
  }

  @PostMapping("/task/{taskId}")
  public ResponseMeta<String> startTask(@PathVariable("taskId") String taskId) {
    String recordId = taskInfoService.startTask(taskId);
    return new ResponseMeta<>(ErrorCode.SUCCESS, recordId);
  }
}
