package com.zj.master.notify.event;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.domain.entity.bo.feature.ExecuteRecordBO;
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
public class CreateExecuteRecordEvent implements INotifyEvent {

  private final IExecuteRecordRepository executeRecordRepository;

  public CreateExecuteRecordEvent(IExecuteRecordRepository executeRecordRepository) {
    this.executeRecordRepository = executeRecordRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.CREATE_EXECUTE_POINT_RECORD;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive execute record create event id = {} event={}", resultEvent.getExecuteId(),
        JSON.toJSONString(resultEvent.getParams()));
    ExecuteRecordBO executeRecord = JSON.parseObject(JSON.toJSONString(resultEvent.getParams()), ExecuteRecordBO.class);
    return executeRecordRepository.saveRecord(executeRecord);
  }
}
