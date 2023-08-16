package com.zj.feature.rest;

import com.zj.common.model.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.dto.HistoryNodeDto;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.feature.service.TaskRecordService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/devops/feature")
public class TaskRecordRest {
  private final TaskRecordService taskRecordService;

  public TaskRecordRest(TaskRecordService taskRecordService) {
    this.taskRecordService = taskRecordService;
  }

  @GetMapping("/task/records")
  public ResponseMeta<PageSize<TaskRecordDto>> getTaskRecordList(
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskRecordService.getTaskRecordPage(page, size));
  }

  @GetMapping("/task/record/{recordId}")
  public ResponseMeta<TaskRecordDto> getTaskRecordDetail(
      @PathVariable("recordId") String recordId) {
    return new ResponseMeta<TaskRecordDto>(ErrorCode.SUCCESS,
        taskRecordService.getTaskRecord(recordId));
  }

  @GetMapping("/task/record/{recordId}/history")
  public ResponseMeta<List<HistoryNodeDto>> getTaskResult(
      @PathVariable("recordId") String recordId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskRecordService.getTaskResult(recordId));
  }

  @DeleteMapping("/task/record/{recordId}")
  public ResponseMeta<Boolean> getTaskRecordList(@PathVariable("recordId") String recordId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskRecordService.deleteTaskRecord(recordId));
  }

  @PutMapping("/task/stop/{recordId}")
  public ResponseMeta<Boolean> stopTaskRecord(
      @PathVariable("recordId") String recordId) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS,
        taskRecordService.stopTaskRecord(recordId));
  }
}
