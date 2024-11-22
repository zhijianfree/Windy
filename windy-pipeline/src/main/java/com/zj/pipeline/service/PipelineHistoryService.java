package com.zj.pipeline.service;

import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import com.zj.domain.entity.bo.pipeline.NodeStatus;
import com.zj.domain.entity.bo.pipeline.PipelineExecuteInfo;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class PipelineHistoryService {
  private final NodeRecordService recordService;
  private final UniqueIdService uniqueIdService;
  private final IPipelineHistoryRepository pipelineHistoryRepository;

  public PipelineHistoryService(NodeRecordService recordService, UniqueIdService uniqueIdService,
      IPipelineHistoryRepository pipelineHistoryRepository) {
    this.recordService = recordService;
    this.uniqueIdService = uniqueIdService;
    this.pipelineHistoryRepository = pipelineHistoryRepository;
  }

  public PipelineHistoryBO getPipelineHistory(String historyId) {
    return pipelineHistoryRepository.getPipelineHistory(historyId);
  }

  public String createPipelineHistory(PipelineHistoryBO pipelineHistoryBO) {
    String historyId = uniqueIdService.getUniqueId();
    pipelineHistoryBO.setHistoryId(historyId);
    return pipelineHistoryRepository.createPipelineHistory(pipelineHistoryBO) ? historyId : "";
  }

  public List<PipelineHistoryBO> listPipelineHistories(String pipelineId) {
    return pipelineHistoryRepository.listPipelineHistories(pipelineId);
  }

  public PipelineHistoryBO getLatestPipelineHistory(String pipelineId) {
    return pipelineHistoryRepository.getLatestPipelineHistory(pipelineId);
  }

  public PipelineExecuteInfo getPipeLineStatusDetail(String historyId) {
    PipelineHistoryBO pipelineHistory = getPipelineHistory(historyId);
    List<NodeRecordBO> nodeRecords = recordService.getNodeRecordsByHistory(historyId);
    List<NodeStatus> statusList = nodeRecords.stream().map(nodeRecord -> {
      NodeStatus nodeStatus = new NodeStatus();
      nodeStatus.setRecordId(nodeRecord.getRecordId());
      nodeStatus.setNodeId(nodeRecord.getNodeId());
      nodeStatus.setStatus(nodeRecord.getStatus());
      nodeStatus.setMessage(nodeRecord.getResult());
      return nodeStatus;
    }).collect(Collectors.toList());

    return PipelineExecuteInfo.builder().pipelineStatus(pipelineHistory.getPipelineStatus())
        .nodeStatusList(statusList).build();
  }
}
