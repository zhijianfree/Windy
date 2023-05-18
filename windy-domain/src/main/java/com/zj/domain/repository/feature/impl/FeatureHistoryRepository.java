package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.po.feature.FeatureHistory;
import com.zj.domain.mapper.feeature.FeatureHistoryMapper;
import com.zj.domain.repository.feature.IExecuteRecordRepository;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.ITestCaseRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * @author falcon
 * @since 2023/5/17
 */
@Slf4j
@Repository
public class FeatureHistoryRepository extends
    ServiceImpl<FeatureHistoryMapper, FeatureHistory> implements IFeatureHistoryRepository {

  @Autowired
  private ITestCaseRepository testCaseRepository;

  @Autowired
  private IExecuteRecordRepository executeRecordRepository;

  @Override
  public List<FeatureHistoryDto> featureHistories(String featureId) {
    List<FeatureHistory> featureHistories = list(
        Wrappers.lambdaQuery(FeatureHistory.class).eq(FeatureHistory::getFeatureId, featureId)
            .orderByDesc(FeatureHistory::getCreateTime));

    if (CollectionUtils.isEmpty(featureHistories)) {
      return Collections.emptyList();
    }

    return featureHistories.stream()
        .map(history -> OrikaUtil.convert(history, FeatureHistoryDto.class))
        .collect(Collectors.toList());
  }

  @Override
  public boolean saveHistory(FeatureHistoryDto featureHistory) {
    FeatureHistory history = OrikaUtil.convert(featureHistory, FeatureHistory.class);
    long dateNow = System.currentTimeMillis();
    history.setCreateTime(dateNow);
    history.setUpdateTime(dateNow);
    return save(history);
  }

  @Override
  public boolean deleteHistory(String historyId) {
    boolean deleteRecode = executeRecordRepository.deleteByHistoryId(historyId);
    boolean result = remove(
        Wrappers.lambdaQuery(FeatureHistory.class).eq(FeatureHistory::getHistoryId, historyId));
    log.info("delete recode={} delete history={}", deleteRecode, result);
    return result && deleteRecode;
  }

  @Override
  public List<FeatureHistoryDto> getHistoriesByTaskId(String taskId) {
    List<FeatureHistory> histories = list(
        Wrappers.lambdaQuery(FeatureHistory.class).eq(FeatureHistory::getRecordId, taskId));
    if (CollectionUtils.isEmpty(histories)) {
      return Collections.emptyList();
    }

    return histories.stream().map(history -> OrikaUtil.convert(history, FeatureHistoryDto.class))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public boolean deleteByRecordId(String taskId) {
    List<FeatureHistory> histories = list(
        Wrappers.lambdaQuery(FeatureHistory.class).eq(FeatureHistory::getRecordId, taskId));
    List<String> historyIds = histories.stream().map(FeatureHistory::getHistoryId)
        .collect(Collectors.toList());
    boolean deleteRecode = executeRecordRepository.batchDeleteByHistoryId(historyIds);

    if (CollectionUtils.isEmpty(historyIds)) {
      return deleteRecode;
    }

    boolean result = remove(
        Wrappers.lambdaQuery(FeatureHistory.class).in(FeatureHistory::getHistoryId, historyIds));
    log.info("delete testCase history  testcaseId={} history={} recode={} delete ", taskId,
        result, deleteRecode);
    return result && deleteRecode;
  }

  @Override
  public boolean updateStatus(String historyId, int status) {
    FeatureHistory featureHistory = new FeatureHistory();
    featureHistory.setHistoryId(historyId);
    featureHistory.setExecuteStatus(status);
    return update(featureHistory,
        Wrappers.lambdaUpdate(FeatureHistory.class).eq(FeatureHistory::getHistoryId, historyId));
  }

  @Override
  public List<FeatureHistoryDto> getTaskRecordFeatures(String taskRecordId) {
    List<FeatureHistory> histories = list(
        Wrappers.lambdaQuery(FeatureHistory.class).eq(FeatureHistory::getRecordId, taskRecordId));
    return OrikaUtil.convertList(histories, FeatureHistoryDto.class);
  }
}
