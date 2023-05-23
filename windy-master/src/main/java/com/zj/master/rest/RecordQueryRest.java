package com.zj.master.rest;

import com.alibaba.fastjson.JSONObject;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ResponseMeta;
import com.zj.common.model.ResponseStatusModel;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.master.service.RecordQueryService;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Autowired
  private RecordQueryService recordQueryService;

  @GetMapping("/record/{recordId}")
  private ResponseMeta<NodeRecordDto> createTask(@PathVariable("recordId") String recordId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, recordQueryService.getRecord(recordId));
  }

  @GetMapping("/task/{taskRecordId}")
  private ResponseStatusModel getTaskStatus(@PathVariable("taskRecordId") String taskRecordId) {
    return recordQueryService.getTaskStatus(taskRecordId);
  }


}
