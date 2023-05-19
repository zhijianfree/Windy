package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.dto.pipeline.NodeStatus;
import com.zj.domain.entity.dto.pipeline.PipelineDTO;
import com.zj.domain.entity.dto.pipeline.PipelineExecuteInfo;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.entity.po.pipeline.PipelineHistory;
import com.zj.domain.mapper.pipeline.PipelineHistoryMapper;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2021/10/15
 */
@Slf4j
@Service
public class PipelineHistoryService {

  @Autowired
  private PipelineService pipelineService;

  @Autowired
  private NodeRecordService recordService;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private IPipelineHistoryRepository pipelineHistoryRepository;

  public PipelineHistoryDto getPipelineHistory(String historyId) {
    return pipelineHistoryRepository.getPipelineHistory(historyId);
  }

  public String createPipelineHistory(PipelineHistoryDto pipelineHistoryDto) {
    String historyId = uniqueIdService.getUniqueId();
    pipelineHistoryDto.setHistoryId(historyId);
    return pipelineHistoryRepository.createPipelineHistory(pipelineHistoryDto) ? historyId : "";
  }

  public List<PipelineHistoryDto> listPipelineHistories(String pipelineId) {
    return pipelineHistoryRepository.listPipelineHistories(pipelineId);
  }

  public PipelineHistoryDto getLatestPipelineHistory(String pipelineId) {
    return pipelineHistoryRepository.getLatestPipelineHistory(pipelineId);
  }

  public void updateStatus(String historyId, ProcessStatus processStatus) {
    pipelineHistoryRepository.updateStatus(historyId, processStatus);
  }

  public PipelineExecuteInfo getPipeLineStatusDetail(String historyId) {
    PipelineHistoryDto pipelineHistory = getPipelineHistory(historyId);
    List<NodeRecordDto> nodeRecords = recordService.getNodeRecordsByHistory(historyId);
    List<NodeStatus> statusList = nodeRecords.stream().map(nodeRecord -> {
      NodeStatus nodeStatus = new NodeStatus();
      nodeStatus.setRecordId(nodeRecord.getRecordId());
      nodeStatus.setNodeId(nodeRecord.getNodeId());
      nodeStatus.setStatus(nodeRecord.getStatus());
      nodeStatus.setMessage(JSON.parseArray(nodeRecord.getResult(), String.class));
      return nodeStatus;
    }).collect(Collectors.toList());

    return PipelineExecuteInfo.builder().pipelineStatus(pipelineHistory.getPipelineStatus())
        .nodeStatusList(statusList).build();
  }
}
