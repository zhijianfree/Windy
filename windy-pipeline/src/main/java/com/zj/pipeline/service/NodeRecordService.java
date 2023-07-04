package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2022/5/24
 */
@Slf4j
@Component
public class NodeRecordService {

  private INodeRecordRepository nodeRecordRepository;

  public static final String APPROVAL_TIPS = "审核通过";

  public NodeRecordService(INodeRecordRepository nodeRecordRepository) {
    this.nodeRecordRepository = nodeRecordRepository;
  }

  public void updateNodeRecordStatus(String recordId, Integer type, String message) {
    boolean update = nodeRecordRepository.updateNodeRecordStatus(recordId, type, message);
    log.info("update result={}", update);
  }

  public Boolean approval(String historyId, String nodeId, Integer type) {
    ProcessStatus processStatus = ProcessStatus.exchange(type);
    if (Objects.isNull(processStatus)) {
      log.warn("can not parse status type ={}", type);
      throw new ApiException(ErrorCode.PARAM_VALIDATE_ERROR);
    }
    NodeRecordDto nodeRecord = nodeRecordRepository.getRecordByNodeAndHistory(historyId, nodeId);
    log.info("get approval recordId={}", nodeRecord.getNodeId());
    updateNodeRecordStatus(nodeRecord.getRecordId(), ProcessStatus.SUCCESS.getType(),
        JSON.toJSONString(Collections.singletonList(APPROVAL_TIPS)));
    return true;
  }

  public List<NodeRecordDto> getNodeRecordsByHistory(String historyId) {
    return nodeRecordRepository.getRecordsByHistoryId(historyId);
  }
}
