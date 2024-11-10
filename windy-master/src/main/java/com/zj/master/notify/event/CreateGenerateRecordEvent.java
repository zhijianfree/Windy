package com.zj.master.notify.event;

import com.alibaba.fastjson.JSON;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.common.entity.generate.GenerateDetail;
import com.zj.common.entity.generate.GenerateRecordBO;
import com.zj.common.enums.NotifyType;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.repository.service.IGenerateRecordRepository;
import com.zj.master.notify.INotifyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class CreateGenerateRecordEvent implements INotifyEvent {

  private final IGenerateRecordRepository generateRecordRepository;

  public CreateGenerateRecordEvent(IGenerateRecordRepository generateRecordRepository) {
    this.generateRecordRepository = generateRecordRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.CREATE_GENERATE_MAVEN;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    log.info("receive generate create record ={}", JSON.toJSONString(resultEvent.getParams()));
    GenerateRecordBO recordDto = JSON.parseObject(JSON.toJSONString(resultEvent.getParams()), GenerateRecordBO.class);
    if (Objects.isNull(recordDto)) {
      return false;
    }
    List<GenerateRecordBO> generateRecord = generateRecordRepository.getGenerateRecord(recordDto.getServiceId(), recordDto.getVersion());
    if (Objects.isNull(generateRecord)){
      GenerateDetail generateDetail = recordDto.getGenerateParams();
      recordDto.setVersion(generateDetail.getVersion());
      return generateRecordRepository.create(recordDto);
    }
    return true;
  }
}
