package com.zj.domain.repository.service;

import com.zj.common.entity.generate.GenerateRecordBO;
import java.util.List;

public interface IGenerateRecordRepository {

  List<GenerateRecordBO> getServiceRecords(String serviceId);

  boolean create(GenerateRecordBO generateRecordBO);

  boolean update(GenerateRecordBO generateRecordBO);

  GenerateRecordBO getGenerateRecord(String serviceId, String version);
}
