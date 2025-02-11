package com.zj.feature.service;

import com.zj.domain.entity.bo.feature.ExecuteRecordBO;
import com.zj.domain.repository.feature.IExecuteRecordRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExecuteRecordService {

  private final IExecuteRecordRepository executeRecordRepository;

  public ExecuteRecordService(IExecuteRecordRepository executeRecordRepository) {
    this.executeRecordRepository = executeRecordRepository;
  }

  public List<ExecuteRecordBO> getExecuteRecords(String historyId) {
    return executeRecordRepository.getExecuteRecords(historyId);
  }

  public void save(ExecuteRecordBO executeRecord) {
    executeRecordRepository.saveRecord(executeRecord);
  }
}
