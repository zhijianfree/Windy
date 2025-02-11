package com.zj.pipeline.service;

import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import com.zj.domain.entity.bo.pipeline.NodeStatus;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.pipeline.PipelineExecuteInfo;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
  private final IPipelineRepository pipelineRepository;
  private final IPipelineHistoryRepository pipelineHistoryRepository;

  public PipelineHistoryService(NodeRecordService recordService, UniqueIdService uniqueIdService,
                                IPipelineRepository pipelineRepository, IPipelineHistoryRepository pipelineHistoryRepository) {
    this.recordService = recordService;
    this.uniqueIdService = uniqueIdService;
    this.pipelineRepository = pipelineRepository;
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
    checkPipeline(pipelineId);
    return pipelineHistoryRepository.listPipelineHistories(pipelineId);
  }

  public PipelineHistoryBO getLatestPipelineHistory(String serviceId, String pipelineId) {
    checkPipelineWithService(serviceId, pipelineId);
    return pipelineHistoryRepository.getLatestPipelineHistory(pipelineId);
  }

  private void checkPipelineWithService(String serviceId, String pipelineId) {
    PipelineBO pipelineBO = checkPipeline(pipelineId);
    if (!Objects.equals(pipelineBO.getServiceId(), serviceId)) {
      log.info("pipeline={} not belong serviceId={}", pipelineId, serviceId);
      throw new ApiException(ErrorCode.PIPELINE_NOT_BELONG_SERVICE);
    }
  }

  private PipelineBO checkPipeline(String pipelineId) {
    PipelineBO pipelineBO = pipelineRepository.getPipeline(pipelineId);
    if (Objects.isNull(pipelineBO)) {
      log.info("can not find pipeline={}", pipelineId);
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }
    return pipelineBO;
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

  public boolean deleteByPipelineId(String pipelineId) {
    checkPipeline(pipelineId);
    return pipelineHistoryRepository.deleteByPipelineId(pipelineId);
  }
}
