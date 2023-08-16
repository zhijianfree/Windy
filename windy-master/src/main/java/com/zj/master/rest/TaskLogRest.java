package com.zj.master.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.DispatchTaskModel;
import com.zj.common.model.ResponseMeta;
import com.zj.master.service.TaskLogService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/5/11
 */
@RestController
@RequestMapping("/v1/devops/dispatch")
public class TaskLogRest {

  private final TaskLogService taskLogService;

  public TaskLogRest(TaskLogService taskLogService) {
    this.taskLogService = taskLogService;
  }

  @PostMapping("/task")
  private ResponseMeta<String> createTask(@RequestBody DispatchTaskModel task) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskLogService.createTask(task));
  }

  @PostMapping("/stop")
  private ResponseMeta<Boolean> stopTask(@RequestBody DispatchTaskModel task) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskLogService.pauseTask(task));
  }
}
