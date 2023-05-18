package com.zj.master.notify;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.model.ResultEvent;
import com.zj.domain.entity.dto.feature.ExecuteRecordDto;
import com.zj.domain.repository.feature.IExecuteRecordRepository;
import com.zj.master.dispatch.feature.FeatureExecuteProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/5/17
 */
@Slf4j
@Component
public class CreateExecuteRecordEvent implements INotifyEvent {

  @Autowired
  private IExecuteRecordRepository executeRecordRepository;

  @Override
  public NotifyType type() {
    return NotifyType.CREATE_EXECUTE_POINT_RECORD;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive node record create event id = {} event={}", resultEvent.getExecuteId(),
        JSON.toJSONString(resultEvent.getParams()));
    ExecuteRecordDto executeRecordDto = JSON.parseObject(JSON.toJSONString(resultEvent.getParams()),
        ExecuteRecordDto.class);
    return executeRecordRepository.saveRecord(executeRecordDto);
  }
}
