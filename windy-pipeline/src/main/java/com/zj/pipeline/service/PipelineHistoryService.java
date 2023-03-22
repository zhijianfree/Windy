package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zj.pipeline.entity.dto.PipelineDTO;
import com.zj.pipeline.entity.dto.PipelineHistoryDto;
import com.zj.pipeline.entity.po.PipelineHistory;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.pipeline.mapper.PipelineHistoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author falcon
 * @since 2021/10/15
 */
@Service
public class PipelineHistoryService {

  @Autowired
  private PipelineService pipelineService;

  @Autowired
  private PipelineHistoryMapper historyMapper;

  public PipelineHistoryDto getPipelineHistory(String historyId) {
    PipelineHistory pipelineHistory = historyMapper.selectOne(
        Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getHistoryId, historyId));

    if (Objects.isNull(pipelineHistory)) {
      return null;
    }

    return PipelineHistoryDto.toPipelineHistoryDto(pipelineHistory);
  }

  public String createPipelineHistory(PipelineHistoryDto pipelineHistoryDto) {
    PipelineHistory history = PipelineHistoryDto.toPipelineHistory(pipelineHistoryDto);
    history.setHistoryId(UUID.randomUUID().toString());
    history.setCreateTime(System.currentTimeMillis());
    history.setUpdateTime(System.currentTimeMillis());
    int result = historyMapper.insert(history);
    if (result > 0){
      return history.getHistoryId();
    }

    return "";
  }

  public List<PipelineHistoryDto> listPipelineHistories(String pipelineId) {
    PipelineDTO pipeline = pipelineService.getPipeline(pipelineId);
    if (Objects.isNull(pipeline)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }

    List<PipelineHistory> pipelineHistories = historyMapper.selectList(
        Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getPipelineId, pipelineId));

    if (CollectionUtils.isEmpty(pipelineHistories)){
      return Collections.emptyList();
    }

    return pipelineHistories.stream().map(PipelineHistoryDto::toPipelineHistoryDto).collect(
        Collectors.toList());
  }

  public PipelineHistoryDto getLatestPipelineHistory(String service, String pipelineId) {
    PipelineHistory pipelineHistory = historyMapper.selectOne(Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getPipelineId, pipelineId).orderByDesc(PipelineHistory::getUpdateTime).last("limit 1"));
    if (Objects.isNull(pipelineHistory)){
      return null;
    }

    return PipelineHistoryDto.toPipelineHistoryDto(pipelineHistory);
  }
}
