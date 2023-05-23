package com.zj.master.dispatch.task;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.log.IDispatchLogRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Slf4j
@Component
public class TaskEndProcessor {

  @Autowired
  private ITaskRecordRepository taskRecordRepository;

  @Autowired
  private IFeatureHistoryRepository featureHistoryRepository;

  @Autowired
  private IFeatureRepository featureRepository;

  @Autowired
  private IDispatchLogRepository taskLogRepository;

  public boolean process(String taskRecordId, ProcessStatus status, String logId) {
    TaskRecordDto taskRecord = taskRecordRepository.getTaskRecord(taskRecordId);
    if (Objects.isNull(taskRecord)) {
      //如果找不到任务记录，可能是任务删除了或者是Id为临时记录Id，无任何业务含义
      log.info("can not find record={} maybe is a temp recordId", taskRecordId);
      return false;
    }

    if (status.isFailStatus()) {
      taskRecordRepository.updateRecordStatus(taskRecordId, status.getType());
      taskLogRepository.updateLogStatus(logId, status.getType());
      return true;
    }

    //1 找到任务记录关联的所有用例
    List<FeatureInfoDto> features = featureRepository.queryNotContainFolder(taskRecord.getTestCaseId());
    //2 找到任务关联所有用例的执行记录(找到的记录都是成功的，否则不会进入到当前逻辑)
    List<String> recordFeatureIds = featureHistoryRepository.getTaskRecordFeatures(taskRecordId)
        .stream().map(FeatureHistoryDto::getFeatureId).collect(Collectors.toList());
    //3 如果所有用例的执行记录都成功那么整个任务执行就成功
    boolean allSuccess = features.stream()
        .allMatch(feature -> recordFeatureIds.contains(feature.getFeatureId()));
    if (allSuccess) {
      int success = ProcessStatus.SUCCESS.getType();
      taskLogRepository.updateLogStatus(logId, success);
      return taskRecordRepository.updateRecordStatus(taskRecordId, success);
    }

    return false;
  }
}
