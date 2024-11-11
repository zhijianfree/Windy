package com.zj.pipeline.service;

import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.zj.pipeline.entity.dto.ApprovalInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Slf4j
@Component
public class NodeRecordService {

  private final INodeRecordRepository nodeRecordRepository;

  public NodeRecordService(INodeRecordRepository nodeRecordRepository) {
    this.nodeRecordRepository = nodeRecordRepository;
  }

  public boolean updateNodeRecordStatus(String recordId, Integer type, List<String> messageList) {
    return nodeRecordRepository.updateNodeRecordStatus(recordId, type, messageList);
  }

  public Boolean approval(ApprovalInfo approvalInfo) {
    ProcessStatus processStatus = ProcessStatus.exchange(approvalInfo.getType());
    if (Objects.isNull(processStatus)) {
      log.warn("can not parse status type ={}", approvalInfo.getType());
      throw new ApiException(ErrorCode.PARAM_VALIDATE_ERROR);
    }
    NodeRecordBO nodeRecord = nodeRecordRepository.getRecordByNodeAndHistory(approvalInfo.getHistoryId(),
            approvalInfo.getNodeId());
    if (Objects.isNull(nodeRecord)) {
      log.info("can not find node record historyId={} nodeId={}",approvalInfo.getHistoryId(), approvalInfo.getNodeId());
      return false;
    }
    return updateNodeRecordStatus(nodeRecord.getRecordId(), processStatus.getType(),
            Collections.singletonList(approvalInfo.getMessage()));
  }

  public List<NodeRecordBO> getNodeRecordsByHistory(String historyId) {
    return nodeRecordRepository.getRecordsByHistoryId(historyId);
  }
}
