package com.zj.master.notify.event;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.model.ResultEvent;
import com.zj.domain.entity.dto.feature.ExecuteRecordDto;
import com.zj.domain.repository.feature.IExecuteRecordRepository;
import com.zj.master.notify.INotifyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class UpdatePointRecordNotifyEvent implements INotifyEvent {

  private final IExecuteRecordRepository executeRecordRepository;

  public UpdatePointRecordNotifyEvent(IExecuteRecordRepository executeRecordRepository) {
    this.executeRecordRepository = executeRecordRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_EXECUTE_POINT_RECORD;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive execute record create event id = {} event={}", resultEvent.getExecuteId(),
        JSON.toJSONString(resultEvent.getParams()));
    ExecuteRecordDto executeRecordDto = JSON.parseObject(JSON.toJSONString(resultEvent.getParams()),
        ExecuteRecordDto.class);
    return executeRecordRepository.updateStatusAndResult(executeRecordDto);
  }
}
