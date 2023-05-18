package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.PipelineDTO;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.domain.entity.po.pipeline.PipelineHistory;
import com.zj.domain.mapper.pipeline.PipelineHistoryMapper;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Repository
public class PipelineHistoryRepository extends
    ServiceImpl<PipelineHistoryMapper, PipelineHistory> implements IPipelineHistoryRepository {

  public static final String LAST_SQL = "limit 1";
  @Autowired
  private IPipelineRepository pipelineRepository;
  @Override
  public PipelineHistoryDto getPipelineHistory(String historyId) {
    PipelineHistory pipelineHistory = getOne(
        Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getHistoryId, historyId));
    if (Objects.isNull(pipelineHistory)) {
      return null;
    }

    return OrikaUtil.convert(pipelineHistory, PipelineHistoryDto.class);
  }

  @Override
  public boolean createPipelineHistory(PipelineHistoryDto pipelineHistoryDto) {
    PipelineHistory history = OrikaUtil.convert(pipelineHistoryDto, PipelineHistory.class);
    history.setCreateTime(System.currentTimeMillis());
    history.setUpdateTime(System.currentTimeMillis());
    return save(history);
  }

  @Override
  public List<PipelineHistoryDto> listPipelineHistories(String pipelineId) {
    PipelineDTO pipeline = pipelineRepository.getPipeline(pipelineId);
    if (Objects.isNull(pipeline)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }

    List<PipelineHistory> pipelineHistories = list(
        Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getPipelineId, pipelineId));

    if (CollectionUtils.isEmpty(pipelineHistories)) {
      return Collections.emptyList();
    }

    return pipelineHistories.stream()
        .map(pipelineHistory -> OrikaUtil.convert(pipelineHistory, PipelineHistoryDto.class))
        .collect(Collectors.toList());
  }

  @Override
  public PipelineHistoryDto getLatestPipelineHistory(String pipelineId) {
    PipelineHistory pipelineHistory = getOne(
        Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getPipelineId, pipelineId)
            .orderByDesc(PipelineHistory::getUpdateTime).last(LAST_SQL));
    if (Objects.isNull(pipelineHistory)) {
      return null;
    }

    return OrikaUtil.convert(pipelineHistory, PipelineHistoryDto.class);
  }

  @Override
  public boolean updateStatus(String historyId, ProcessStatus processStatus) {
    PipelineHistory pipelineHistory = new PipelineHistory();
    pipelineHistory.setHistoryId(historyId);
    pipelineHistory.setPipelineStatus(processStatus.getType());
    pipelineHistory.setUpdateTime(System.currentTimeMillis());
    return update(pipelineHistory,
        Wrappers.lambdaUpdate(PipelineHistory.class).eq(PipelineHistory::getHistoryId, historyId));
  }
}
