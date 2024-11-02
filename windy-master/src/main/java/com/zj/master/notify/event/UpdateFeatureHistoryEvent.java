package com.zj.master.notify.event;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.master.dispatch.task.FeatureExecuteProxy;
import com.zj.master.notify.INotifyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class UpdateFeatureHistoryEvent implements INotifyEvent {

  private final IFeatureHistoryRepository featureHistoryRepository;
  private final FeatureExecuteProxy featureExecuteProxy;
  private final ISubDispatchLogRepository subTaskLogRepository;

  public UpdateFeatureHistoryEvent(IFeatureHistoryRepository featureHistoryRepository,
      FeatureExecuteProxy featureExecuteProxy, ISubDispatchLogRepository subTaskLogRepository) {
    this.featureHistoryRepository = featureHistoryRepository;
    this.featureExecuteProxy = featureExecuteProxy;
    this.subTaskLogRepository = subTaskLogRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_FEATURE_HISTORY;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive update feature history event id = {} event={}", resultEvent.getExecuteId(),
        resultEvent.getExecuteType());
    FeatureHistoryBO history = JSON.parseObject(JSON.toJSONString(resultEvent.getParams()), FeatureHistoryBO.class);
    FeatureHistoryBO featureHistory = featureHistoryRepository.getFeatureHistory(
        history.getHistoryId());
    if (ProcessStatus.isCompleteStatus(featureHistory.getExecuteStatus())) {
      log.info("feature history status completed,not update historyId={}", history.getHistoryId());
      return true;
    }

    boolean updateStatus = featureHistoryRepository.updateStatus(history.getHistoryId(),
        resultEvent.getStatus().getType());
    subTaskLogRepository.updateLogStatus(resultEvent.getLogId(), history.getHistoryId(),
        history.getExecuteStatus());

    Map<String, Object> context = resultEvent.getContext();
    featureExecuteProxy.featureStatusChange(resultEvent.getExecuteId(), history, context);
    return updateStatus;
  }
}
