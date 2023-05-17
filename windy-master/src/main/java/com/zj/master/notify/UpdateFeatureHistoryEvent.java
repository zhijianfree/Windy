package com.zj.master.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.model.ResultEvent;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/5/17
 */
@Slf4j
@Component
public class UpdateFeatureHistoryEvent implements INotifyEvent {

  @Autowired
  private IFeatureHistoryRepository featureHistoryRepository;

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_FEATURE_HISTORY;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive node record create event id = {} event={}", resultEvent.getExecuteId(),
        JSON.toJSONString(resultEvent.getParams()));
    return featureHistoryRepository.updateStatus(resultEvent.getExecuteId(),
        resultEvent.getStatus().getType());
  }
}
