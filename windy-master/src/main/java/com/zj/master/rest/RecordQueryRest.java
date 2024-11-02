package com.zj.master.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.entity.dto.ResponseStatusModel;
import com.zj.domain.entity.bo.pipeline.NodeRecordDto;
import com.zj.master.service.RecordQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/5/11
 */
@RestController
@RequestMapping("/v1/devops/master")
public class RecordQueryRest {

  private final RecordQueryService recordQueryService;

  public RecordQueryRest(RecordQueryService recordQueryService) {
    this.recordQueryService = recordQueryService;
  }

  @GetMapping("/record/{recordId}")
  public ResponseMeta<NodeRecordDto> createTask(@PathVariable("recordId") String recordId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, recordQueryService.getRecord(recordId));
  }

  @GetMapping("/task/{taskRecordId}/status")
  public ResponseStatusModel getTaskStatus(@PathVariable("taskRecordId") String taskRecordId) {
    return recordQueryService.getTaskStatus(taskRecordId);
  }


}
