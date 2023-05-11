package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.pipeline.entity.dto.NodeStatus;
import com.zj.pipeline.entity.dto.PipelineDTO;
import com.zj.pipeline.entity.dto.PipelineExecuteInfo;
import com.zj.pipeline.entity.dto.PipelineHistoryDto;
import com.zj.pipeline.entity.po.NodeRecord;
import com.zj.pipeline.entity.po.PipelineHistory;
import com.zj.pipeline.mapper.PipelineHistoryMapper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author falcon
 * @since 2021/10/15
 */
@Slf4j
@Service
public class PipelineHistoryService extends ServiceImpl<PipelineHistoryMapper, PipelineHistory> {

  @Autowired
  private PipelineService pipelineService;

  @Autowired
  private NodeRecordService recordService;

  @Autowired
  private UniqueIdService uniqueIdService;

  public PipelineHistoryDto getPipelineHistory(String historyId) {
    PipelineHistory pipelineHistory = getOne(
        Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getHistoryId, historyId));
    if (Objects.isNull(pipelineHistory)) {
      return null;
    }

    return PipelineHistoryDto.toPipelineHistoryDto(pipelineHistory);
  }

  public String createPipelineHistory(PipelineHistoryDto pipelineHistoryDto) {
    PipelineHistory history = PipelineHistoryDto.toPipelineHistory(pipelineHistoryDto);
    history.setHistoryId(uniqueIdService.getUniqueId());
    history.setCreateTime(System.currentTimeMillis());
    history.setUpdateTime(System.currentTimeMillis());
    return save(history) ? history.getHistoryId() : "";
  }

  public List<PipelineHistoryDto> listPipelineHistories(String pipelineId) {
    PipelineDTO pipeline = pipelineService.getPipeline(pipelineId);
    if (Objects.isNull(pipeline)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }

    List<PipelineHistory> pipelineHistories = list(
        Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getPipelineId, pipelineId));

    if (CollectionUtils.isEmpty(pipelineHistories)) {
      return Collections.emptyList();
    }

    return pipelineHistories.stream().map(PipelineHistoryDto::toPipelineHistoryDto)
        .collect(Collectors.toList());
  }

  public PipelineHistoryDto getLatestPipelineHistory(String pipelineId) {
    PipelineHistory pipelineHistory = getOne(
        Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getPipelineId, pipelineId)
            .orderByDesc(PipelineHistory::getUpdateTime).last("limit 1"));
    if (Objects.isNull(pipelineHistory)) {
      return null;
    }

    return PipelineHistoryDto.toPipelineHistoryDto(pipelineHistory);
  }

  public void updateStatus(String historyId, ProcessStatus processStatus) {
    PipelineHistory pipelineHistory = new PipelineHistory();
    pipelineHistory.setHistoryId(historyId);
    pipelineHistory.setPipelineStatus(processStatus.getType());
    pipelineHistory.setUpdateTime(System.currentTimeMillis());
    boolean result = update(pipelineHistory,
        Wrappers.lambdaUpdate(PipelineHistory.class).eq(PipelineHistory::getHistoryId, historyId));
  }

  public PipelineExecuteInfo getPipeLineStatusDetail(String historyId) {
    PipelineHistoryDto pipelineHistory = getPipelineHistory(historyId);
    List<NodeRecord> nodeRecords = recordService.list(Wrappers.lambdaQuery(NodeRecord.class)
        .eq(NodeRecord::getHistoryId, historyId));
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
