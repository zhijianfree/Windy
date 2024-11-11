package com.zj.pipeline.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.pipeline.entity.dto.ApprovalInfo;
import com.zj.pipeline.service.NodeRecordService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@RestController
@RequestMapping("/v1/devops/pipeline")
public class NodeRecordRest {
  private final NodeRecordService nodeRecordService;

  public NodeRecordRest(NodeRecordService nodeRecordService) {
    this.nodeRecordService = nodeRecordService;
  }

  @PutMapping("/node/approval")
  public ResponseMeta<Boolean> approvalNode(@RequestBody ApprovalInfo approvalInfo) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, nodeRecordService.approval(approvalInfo));
  }
}
