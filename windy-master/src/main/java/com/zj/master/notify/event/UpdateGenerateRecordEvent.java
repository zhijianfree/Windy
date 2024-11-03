package com.zj.master.notify.event;

import com.zj.common.enums.NotifyType;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.entity.generate.GenerateRecordBO;
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
    GenerateRecordBO recordDto = OrikaUtil.convert(resultEvent.getParams(), GenerateRecordBO.class);
    if (Objects.isNull(recordDto)) {
      return false;
    }

    GenerateRecordBO update = new GenerateRecordBO();
    update.setRecordId(resultEvent.getExecuteId());
    update.setGenerateResult(recordDto.getGenerateResult());
    update.setStatus(resultEvent.getStatus().getType());
    return generateRecordRepository.update(update);
  }
}
