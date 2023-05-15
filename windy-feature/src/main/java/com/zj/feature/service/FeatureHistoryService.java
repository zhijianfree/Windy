package com.zj.feature.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.utils.OrikaUtil;
import com.zj.feature.entity.dto.FeatureHistoryDTO;
import com.zj.feature.entity.dto.FeatureInfoDTO;
import com.zj.domain.entity.po.feature.FeatureHistory;
import com.zj.feature.entity.type.ExecuteStatusEnum;
import com.zj.domain.mapper.feeature.FeatureHistoryMapper;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class FeatureHistoryService extends ServiceImpl<FeatureHistoryMapper, FeatureHistory> {

  @Autowired
  private ExecuteRecordService executeRecordService;

  @Autowired
  private FeatureService featureService;

  public List<FeatureHistoryDTO> featureHistories(String featureId) {
    List<FeatureHistory> featureHistories = list(
        Wrappers.lambdaQuery(FeatureHistory.class).eq(FeatureHistory::getFeatureId, featureId)
            .orderByDesc(FeatureHistory::getCreateTime));

    if (CollectionUtils.isEmpty(featureHistories)) {
      return Collections.emptyList();
    }

    return featureHistories.stream()
        .map(history -> OrikaUtil.convert(history, FeatureHistoryDTO.class))
        .collect(Collectors.toList());
  }

  public boolean insert(FeatureHistory featureHistory) {
    return save(featureHistory);
  }

  public boolean deleteHistory(String historyId) {
    boolean deleteRecode = executeRecordService.deleteByHistoryId(historyId);
    boolean result = remove(
        Wrappers.lambdaQuery(FeatureHistory.class).eq(FeatureHistory::getHistoryId, historyId));
    log.info("delete recode={} delete history={}", deleteRecode, result);
    return result && deleteRecode;
  }

  public List<FeatureHistoryDTO> getHistories(String recordId) {
    List<FeatureHistory> histories = list(
        Wrappers.lambdaQuery(FeatureHistory.class).eq(FeatureHistory::getRecordId, recordId));
    if (CollectionUtils.isEmpty(histories)) {
      return Collections.emptyList();
    }

    return histories.stream().map(history -> OrikaUtil.convert(history, FeatureHistoryDTO.class))
        .collect(Collectors.toList());
  }

  public boolean deleteByRecordId(String recordId) {
    List<FeatureHistory> histories = list(
        Wrappers.lambdaQuery(FeatureHistory.class).eq(FeatureHistory::getRecordId, recordId));
    List<String> historyIds = histories.stream().map(FeatureHistory::getHistoryId)
        .collect(Collectors.toList());
    boolean deleteRecode = executeRecordService.batchDeleteByHistoryId(historyIds);

    if (CollectionUtils.isEmpty(historyIds)) {
      return deleteRecode;
    }

    boolean result = remove(
        Wrappers.lambdaQuery(FeatureHistory.class).in(FeatureHistory::getHistoryId, historyIds));
    log.info("delete testCase history  testcaseId={} history={} recode={} delete ", recordId,
        result, deleteRecode);
    return result && deleteRecode;
  }

  public boolean updateStatus(String historyId, int status) {
    FeatureHistory featureHistory = new FeatureHistory();
    featureHistory.setHistoryId(historyId);
    featureHistory.setExecuteStatus(status);
    return update(featureHistory,
        Wrappers.lambdaUpdate(FeatureHistory.class).eq(FeatureHistory::getHistoryId, historyId));
  }

  public void saveFeatureHistory(String featureId, String historyId, String recordId) {
    FeatureInfoDTO featureInfo = featureService.getFeatureById(featureId);
    if (Objects.isNull(featureInfo)) {
      throw new ApiException(ErrorCode.FEATURE_NOT_FIND);
    }

    FeatureHistory featureHistory = new FeatureHistory();
    featureHistory.setFeatureId(featureId);
    featureHistory.setExecuteStatus(ExecuteStatusEnum.RUNNING.getStatus());
    featureHistory.setHistoryId(historyId);
    featureHistory.setRecordId(recordId);
    featureHistory.setFeatureName(featureInfo.getFeatureName());
    featureHistory.setCreateTime(System.currentTimeMillis());
    insert(featureHistory);
  }
}
