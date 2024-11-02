package com.zj.master.notify.event;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.master.notify.INotifyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class CreateFeatureHistoryEvent implements INotifyEvent {

  private final IFeatureHistoryRepository featureHistoryRepository;
  private final IFeatureRepository featureRepository;
  private final ISubDispatchLogRepository subDispatchLogRepository;

  public CreateFeatureHistoryEvent(IFeatureHistoryRepository featureHistoryRepository,
                                   IFeatureRepository featureRepository, ISubDispatchLogRepository subDispatchLogRepository) {
    this.featureHistoryRepository = featureHistoryRepository;
    this.featureRepository = featureRepository;
    this.subDispatchLogRepository = subDispatchLogRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.CREATE_FEATURE_HISTORY;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive feature history create event id = {} event={}", resultEvent.getExecuteId(),
        JSON.toJSONString(resultEvent.getParams()));
    FeatureHistoryBO featureHistoryBO = JSON.parseObject(
        JSON.toJSONString(resultEvent.getParams()), FeatureHistoryBO.class);
    FeatureInfoBO feature = featureRepository.getFeatureById(featureHistoryBO.getFeatureId());
    Optional.ofNullable(feature).ifPresent(f -> featureHistoryBO.setFeatureName(f.getFeatureName()));

    subDispatchLogRepository.updateSubLogClientIp(resultEvent.getLogId(),
        featureHistoryBO.getFeatureId(), resultEvent.getClientIp());

    featureHistoryBO.setExecuteStatus(resultEvent.getStatus().getType());
    return featureHistoryRepository.saveHistory(featureHistoryBO);
  }
}
