package com.zj.client.rest;

import com.alibaba.fastjson.JSONObject;
import com.zj.client.service.TaskDispatchService;
import com.zj.common.model.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.StopDispatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@RestController
@RequestMapping("/v1/client/task")
public class TaskDispatchRest {

  @Autowired
  private TaskDispatchService taskDispatchService;

  @PostMapping("")
  public ResponseMeta<Boolean> createTask(@RequestBody JSONObject params) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDispatchService.dispatch(params));
  }

  @PutMapping("/stop")
  public ResponseMeta<Boolean> stopTask(@RequestBody StopDispatch stopDispatch) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDispatchService.stopDispatch(stopDispatch));
  }
}
