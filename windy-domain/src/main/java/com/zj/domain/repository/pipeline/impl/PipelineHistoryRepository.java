package com.zj.domain.repository.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.pipeline.PipelineConfig;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.domain.entity.po.pipeline.PipelineHistory;
import com.zj.domain.mapper.pipeline.PipelineHistoryMapper;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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

  private final IPipelineRepository pipelineRepository;

  public PipelineHistoryRepository(IPipelineRepository pipelineRepository) {
    this.pipelineRepository = pipelineRepository;
  }

  @Override
  public PipelineHistoryBO getPipelineHistory(String historyId) {
    PipelineHistory pipelineHistory = getOne(
        Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getHistoryId, historyId));
    if (Objects.isNull(pipelineHistory)) {
      return null;
    }

    return cenvertPipelineHistoryBO(pipelineHistory);
  }

  @Override
  public boolean createPipelineHistory(PipelineHistoryBO pipelineHistoryBO) {
    PipelineHistory history = convertPipelineHistory(pipelineHistoryBO);
    history.setCreateTime(System.currentTimeMillis());
    history.setUpdateTime(System.currentTimeMillis());
    return save(history);
  }

  @Override
  public List<PipelineHistoryBO> listPipelineHistories(String pipelineId) {
    PipelineBO pipeline = pipelineRepository.getPipeline(pipelineId);
    if (Objects.isNull(pipeline)) {
      throw new ApiException(ErrorCode.NOT_FOUND_PIPELINE);
    }

    List<PipelineHistory> pipelineHistories = list(Wrappers.lambdaQuery(PipelineHistory.class)
            .eq(PipelineHistory::getPipelineId, pipelineId)
            .orderByDesc(PipelineHistory::getCreateTime).last("limit 10"));
    if (CollectionUtils.isEmpty(pipelineHistories)) {
      return Collections.emptyList();
    }

    return pipelineHistories.stream()
        .map(PipelineHistoryRepository::cenvertPipelineHistoryBO)
        .collect(Collectors.toList());
  }

  @Override
  public PipelineHistoryBO getLatestPipelineHistory(String pipelineId) {
    PipelineHistory pipelineHistory = getOne(
        Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getPipelineId, pipelineId)
            .orderByDesc(PipelineHistory::getCreateTime).last(LAST_SQL));
    if (Objects.isNull(pipelineHistory)) {
      return null;
    }

    return cenvertPipelineHistoryBO(pipelineHistory);
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

  private static PipelineHistoryBO cenvertPipelineHistoryBO(PipelineHistory pipelineHistory) {
    PipelineHistoryBO pipelineHistoryBO = OrikaUtil.convert(pipelineHistory, PipelineHistoryBO.class);
    pipelineHistoryBO.setPipelineConfig(JSON.parseObject(pipelineHistory.getConfig(), PipelineConfig.class));
    return pipelineHistoryBO;
  }

  private static PipelineHistory convertPipelineHistory(PipelineHistoryBO pipelineHistoryBO) {
    PipelineHistory pipelineHistory = OrikaUtil.convert(pipelineHistoryBO, PipelineHistory.class);
    Optional.ofNullable(pipelineHistoryBO.getPipelineConfig()).ifPresent(config ->
            pipelineHistory.setConfig(JSON.toJSONString(config)));
    return pipelineHistory;
  }

  @Override
  public List<PipelineHistoryBO> getOldPipelineHistory(long queryTime) {
    List<PipelineHistory> pipelineHistories = list(Wrappers.lambdaQuery(PipelineHistory.class).le(PipelineHistory::getCreateTime, queryTime));
    return OrikaUtil.convertList(pipelineHistories, PipelineHistoryBO.class);
  }

  @Override
  public boolean deleteByHistoryId(String historyId) {
    return remove(Wrappers.lambdaQuery(PipelineHistory.class).eq(PipelineHistory::getHistoryId, historyId));
  }
}
