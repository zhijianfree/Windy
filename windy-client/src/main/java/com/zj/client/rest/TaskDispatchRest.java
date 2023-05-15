package com.zj.client.rest;

import com.zj.client.entity.dto.BaseParam;
import com.zj.client.service.TaskDispatchService;
import com.zj.common.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author falcon
 * @since 2023/5/15
 */
@RestController
@RequestMapping("/v1/client/task")
public class TaskDispatchRest {

  @Autowired
  private TaskDispatchService taskDispatchService;

  @PostMapping("")
  public ResponseMeta<Boolean> createTask(@RequestBody BaseParam baseParam) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDispatchService.dispatch(baseParam));
  }
}
