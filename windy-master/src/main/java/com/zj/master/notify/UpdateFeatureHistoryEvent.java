package com.zj.master.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.model.ResultEvent;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.master.dispatch.task.FeatureExecuteProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class UpdateFeatureHistoryEvent implements INotifyEvent {

  @Autowired
  private IFeatureHistoryRepository featureHistoryRepository;

  @Autowired
  private FeatureExecuteProxy featureExecuteProxy;

  @Autowired
  private ISubDispatchLogRepository subTaskLogRepository;

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_FEATURE_HISTORY;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive node record create event id = {} event={}", resultEvent.getExecuteId(),
        JSON.toJSONString(resultEvent.getParams()));
    FeatureHistoryDto history = JSON.parseObject(
        JSON.toJSONString(resultEvent.getParams()), FeatureHistoryDto.class);
    FeatureHistoryDto featureHistory = featureHistoryRepository.getFeatureHistory(
        history.getHistoryId());
    if (ProcessStatus.isCompleteStatus(featureHistory.getExecuteStatus())) {
      log.info("feature history status completed,not update historyId={}", history.getHistoryId());
      return true;
    }

    boolean updateStatus = featureHistoryRepository.updateStatus(history.getHistoryId(),
        resultEvent.getStatus().getType());

    subTaskLogRepository.updateLogStatus(resultEvent.getLogId(), history.getHistoryId(),
        history.getExecuteStatus());

    featureExecuteProxy.featureStatusChange(resultEvent.getExecuteId(), history);
    return updateStatus;
  }
}
