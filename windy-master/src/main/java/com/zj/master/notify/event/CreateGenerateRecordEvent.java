package com.zj.master.notify.event;

import com.zj.common.entity.dto.ResultEvent;
import com.zj.common.entity.generate.GenerateDetail;
import com.zj.common.entity.generate.GenerateRecordBO;
import com.zj.common.enums.NotifyType;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.repository.service.IGenerateRecordRepository;
import com.zj.master.notify.INotifyEvent;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
    GenerateRecordBO recordDto = OrikaUtil.convert(resultEvent.getParams(), GenerateRecordBO.class);
    if (Objects.isNull(recordDto)) {
      return false;
    }
    GenerateDetail generateDetail = recordDto.getGenerateParams();
    recordDto.setVersion(generateDetail.getVersion());
    return generateRecordRepository.create(recordDto);
  }
}
