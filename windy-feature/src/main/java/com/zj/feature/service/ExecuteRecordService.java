package com.zj.feature.service;

import com.zj.domain.entity.dto.feature.ExecuteRecordDto;
import com.zj.domain.repository.feature.IExecuteRecordRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExecuteRecordService {

  private IExecuteRecordRepository executeRecordRepository;

  public ExecuteRecordService(IExecuteRecordRepository executeRecordRepository) {
    this.executeRecordRepository = executeRecordRepository;
  }

  public List<ExecuteRecordDto> getExecuteRecords(String historyId) {
    return executeRecordRepository.getExecuteRecords(historyId);
  }

  public void save(ExecuteRecordDto executeRecord) {
    executeRecordRepository.saveRecord(executeRecord);
  }
}
