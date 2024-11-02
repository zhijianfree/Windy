package com.zj.master.notify.event;

import com.zj.common.enums.NotifyType;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.service.GenerateRecordDto;
import com.zj.domain.repository.service.IGenerateRecordRepository;
import java.util.Objects;

import com.zj.master.notify.INotifyEvent;
import org.springframework.stereotype.Component;

@Component
public class UpdateGenerateRecordEvent implements INotifyEvent {

  private final IGenerateRecordRepository generateRecordRepository;

  public UpdateGenerateRecordEvent(IGenerateRecordRepository generateRecordRepository) {
    this.generateRecordRepository = generateRecordRepository;
  }

  @Override
  public NotifyType type() {
    return NotifyType.UPDATE_GENERATE_MAVEN;
  }

  @Override
  public boolean handle(ResultEvent resultEvent) {
    GenerateRecordDto recordDto = OrikaUtil.convert(resultEvent.getParams(), GenerateRecordDto.class);
    if (Objects.isNull(recordDto)) {
      return false;
    }

    GenerateRecordDto update = new GenerateRecordDto();
    update.setRecordId(resultEvent.getExecuteId());
    update.setResult(recordDto.getResult());
    update.setStatus(resultEvent.getStatus().getType());
    return generateRecordRepository.update(update);
  }
}
