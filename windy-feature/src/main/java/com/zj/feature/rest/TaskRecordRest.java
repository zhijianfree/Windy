package com.zj.feature.rest;

import com.zj.common.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.dto.HistoryNodeDTO;
import com.zj.common.PageSize;
import com.zj.feature.entity.dto.TaskRecordDTO;
import com.zj.feature.service.TaskRecordService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/devops/feature")
public class TaskRecordRest {

  @Autowired
  private TaskRecordService taskRecordService;

  @GetMapping("/task/records")
  public ResponseMeta<PageSize<TaskRecordDTO>> getTaskRecordList(
      @RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskRecordService.getTaskRecordPage(page, size));
  }

  @GetMapping("/task/record/{recordId}")
  public ResponseMeta<TaskRecordDTO> getTaskRecordDetail(
      @PathVariable("recordId") String recordId) {
    return new ResponseMeta<TaskRecordDTO>(ErrorCode.SUCCESS,
        taskRecordService.getTaskRecord(recordId));
  }

  @GetMapping("/task/record/{recordId}/history")
  public ResponseMeta<List<HistoryNodeDTO>> getTaskResult(
      @PathVariable("recordId") String recordId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskRecordService.getTaskResult(recordId));
  }

  @DeleteMapping("/task/record/{recordId}")
  public ResponseMeta<Boolean> getTaskRecordList(@PathVariable("recordId") String recordId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, taskRecordService.deleteTaskRecord(recordId));
  }
}
