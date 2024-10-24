package com.zj.master.notify.event;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.enums.TemplateType;
import com.zj.common.model.ResultEvent;
import com.zj.domain.entity.dto.feature.ExecuteRecordDto;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.repository.feature.IExecuteRecordRepository;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.master.notify.INotifyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class UpdatePointRecordNotifyEvent implements INotifyEvent {

  private final IExecuteRecordRepository executeRecordRepository;
  private final IFeatureHistoryRepository historyRepository;

  public UpdatePointRecordNotifyEvent(IExecuteRecordRepository executeRecordRepository, IFeatureHistoryRepository historyRepository) {
    this.executeRecordRepository = executeRecordRepository;
    this.historyRepository = historyRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_EXECUTE_POINT_RECORD;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive execute record update event id = {} event={}", resultEvent.getExecuteId(),
        JSON.toJSONString(resultEvent.getParams()));
    ExecuteRecordDto executeRecordDto = JSON.parseObject(JSON.toJSONString(resultEvent.getParams()),
        ExecuteRecordDto.class);
    updateFeatureHistory(executeRecordDto);
    return executeRecordRepository.updateStatusAndResult(executeRecordDto);
  }

  private void updateFeatureHistory(ExecuteRecordDto executeRecordDto) {
    if (!Objects.equals(TemplateType.THREAD.getType(), executeRecordDto.getExecuteType())) {
      return;
    }

    //如果用例状态已经是失败状态则不需要处理
    String historyId = executeRecordDto.getHistoryId();
    FeatureHistoryDto featureHistory = historyRepository.getFeatureHistory(historyId);
    if (ProcessStatus.exchange(featureHistory.getExecuteStatus()).isFailStatus()) {
      return;
    }

    // 当前执行点执行的结果为失败状态时才需更新用例状态
    if (ProcessStatus.exchange(executeRecordDto.getStatus()).isFailStatus()) {
      featureHistory.setExecuteStatus(executeRecordDto.getStatus());
      boolean update = historyRepository.updateStatus(executeRecordDto.getHistoryId(), executeRecordDto.getStatus());
      log.info("async execute record update feature history status = {} recordId={} historyId={} result={}",
              executeRecordDto.getStatus(), executeRecordDto.getExecuteRecordId(), historyId, update);
    }
  }
}
