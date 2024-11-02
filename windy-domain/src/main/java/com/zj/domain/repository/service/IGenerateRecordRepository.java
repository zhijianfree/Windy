package com.zj.domain.repository.service;

import com.zj.domain.entity.bo.service.GenerateRecordDto;
import java.util.List;

public interface IGenerateRecordRepository {

  List<GenerateRecordDto> getServiceRecords(String serviceId);

  boolean create(GenerateRecordDto generateRecordDto);

  boolean update(GenerateRecordDto generateRecordDto);

  GenerateRecordDto getGenerateRecord(String serviceId, String version);
}
