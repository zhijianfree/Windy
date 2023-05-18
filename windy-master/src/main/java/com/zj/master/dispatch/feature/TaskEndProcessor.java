package com.zj.master.dispatch.feature;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.feature.ITaskRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/5/18
 */
@Component
public class TaskEndProcessor {

  @Autowired
  private ITaskRecordRepository taskRecordRepository;

  @Autowired
  private IFeatureHistoryRepository featureHistoryRepository;

  @Autowired
  private IFeatureRepository featureRepository;

  public void process(String taskRecordId, ProcessStatus status) {
    if (status.isFailStatus()) {
      taskRecordRepository.updateRecordStatus(taskRecordId, status.getType());
      return;
    }

    //1 找到任务记录关联的所有用例
    TaskRecordDto taskRecord = taskRecordRepository.getTaskRecord(taskRecordId);
    List<FeatureInfoDto> features = featureRepository.queryFeatureList(
        taskRecord.getTestCaseId());
    //2 找到任务关联所有用例的执行记录(找到的记录都是成功的，否则不会进入到当前逻辑)
    List<String> recordFeatureIds = featureHistoryRepository.getTaskRecordFeatures(
        taskRecordId).stream().map(FeatureHistoryDto::getFeatureId).collect(Collectors.toList());
    //3 如果所有用例的执行记录都成功那么整个任务执行就成功
    boolean allSuccess = features.stream()
        .allMatch(feature -> recordFeatureIds.contains(feature.getFeatureId()));
    if (allSuccess) {
      taskRecordRepository.updateRecordStatus(taskRecordId, ProcessStatus.SUCCESS.getType());
    }
  }
}
