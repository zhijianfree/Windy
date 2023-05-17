package com.zj.master.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.master.entity.dto.TaskDetailDto;
import com.zj.master.service.TaskLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author falcon
 * @since 2023/5/11
 */
@RestController
@RequestMapping("/v1/devops/master/record")
public class NodeRecordRest {

  @Autowired
  private INodeRecordRepository nodeRecordRepository;

  @PostMapping("/{recordId}")
  private ResponseMeta<NodeRecord> createTask(@PathVariable("recordId") String recordId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, nodeRecordRepository.getRecordById(recordId));
  }
}
