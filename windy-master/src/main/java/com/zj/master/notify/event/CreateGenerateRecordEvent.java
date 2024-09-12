package com.zj.master.notify.event;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.NotifyType;
import com.zj.common.generate.GenerateDetail;
import com.zj.common.model.ResultEvent;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.service.GenerateRecordDto;
import com.zj.domain.repository.service.IGenerateRecordRepository;
import java.util.Objects;

import com.zj.master.notify.INotifyEvent;
import org.springframework.stereotype.Component;

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
    GenerateRecordDto recordDto = OrikaUtil.convert(resultEvent.getParams(), GenerateRecordDto.class);
    if (Objects.isNull(recordDto)) {
      return false;
    }
    GenerateDetail generateDetail = JSON.parseObject(recordDto.getExecuteParams(), GenerateDetail.class);
    recordDto.setVersion(generateDetail.getVersion());
    return generateRecordRepository.create(recordDto);
  }
}
