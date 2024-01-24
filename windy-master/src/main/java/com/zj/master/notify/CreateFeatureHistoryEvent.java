package com.zj.master.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.model.ResultEvent;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
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
    FeatureHistoryDto featureHistoryDto = JSON.parseObject(
        JSON.toJSONString(resultEvent.getParams()), FeatureHistoryDto.class);
    FeatureInfoDto feature = featureRepository.getFeatureById(featureHistoryDto.getFeatureId());
    Optional.ofNullable(feature).ifPresent(f -> featureHistoryDto.setFeatureName(f.getFeatureName()));

    subDispatchLogRepository.updateSubLogClientIp(resultEvent.getLogId(),
        featureHistoryDto.getFeatureId(), resultEvent.getClientIp());

    featureHistoryDto.setExecuteStatus(resultEvent.getStatus().getType());
    return featureHistoryRepository.saveHistory(featureHistoryDto);
  }
}
