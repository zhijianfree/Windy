package com.zj.master.rest;

import com.zj.common.model.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.master.entity.dto.TaskDetailDto;
import com.zj.master.service.TaskLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author falcon
 * @since 2023/5/11
 */
@RestController
@RequestMapping("/v1/devops/dispatch")
public class TaskLogRest {

  @Autowired
  private TaskLogService taskLogService;

  @PostMapping("/task")
  private ResponseMeta<Boolean> createTask(@RequestBody TaskDetailDto taskDetailDto) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskLogService.createTask(taskDetailDto));
  }
}
