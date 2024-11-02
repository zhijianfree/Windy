package com.zj.client.rest;

import com.zj.client.entity.dto.GenerateDto;
import com.zj.client.handler.feature.executor.vo.FeatureParam;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.service.TaskDispatchService;
import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.entity.dto.StopDispatch;
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
@RequestMapping("/v1/client")
public class TaskDispatchRest {

  private final TaskDispatchService taskDispatchService;

  public TaskDispatchRest(TaskDispatchService taskDispatchService) {
    this.taskDispatchService = taskDispatchService;
  }

  @PostMapping("/dispatch/generate")
  public ResponseMeta<Boolean> dispatchGenerate(@RequestBody GenerateDto generate) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDispatchService.runGenerate(generate));
  }

  @PostMapping("/dispatch/feature")
  public ResponseMeta<Boolean> dispatchFeature(@RequestBody FeatureParam featureParam) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDispatchService.runFeature(featureParam));
  }

  @PostMapping("/dispatch/pipeline")
  public ResponseMeta<Boolean> createTask(@RequestBody TaskNode taskNode) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDispatchService.runPipeline(taskNode));
  }

  @PutMapping("/task/stop")
  public ResponseMeta<Boolean> stopTask(@RequestBody StopDispatch stopDispatch) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskDispatchService.stopDispatch(stopDispatch));
  }
}
