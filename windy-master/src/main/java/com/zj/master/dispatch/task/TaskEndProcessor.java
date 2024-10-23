package com.zj.master.dispatch.task;

import com.zj.common.enums.FeatureStatus;
import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.log.IDispatchLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Slf4j
@Component
public class TaskEndProcessor {

    private final ITaskRecordRepository taskRecordRepository;
    private final IFeatureHistoryRepository featureHistoryRepository;
    private final IFeatureRepository featureRepository;
    private final IDispatchLogRepository taskLogRepository;

    public TaskEndProcessor(ITaskRecordRepository taskRecordRepository,
                            IFeatureHistoryRepository featureHistoryRepository, IFeatureRepository featureRepository,
                            IDispatchLogRepository taskLogRepository) {
        this.taskRecordRepository = taskRecordRepository;
        this.featureHistoryRepository = featureHistoryRepository;
        this.featureRepository = featureRepository;
        this.taskLogRepository = taskLogRepository;
    }

    public boolean process(String taskRecordId, String logId) {
        TaskRecordDto taskRecord = taskRecordRepository.getTaskRecord(taskRecordId);
        if (Objects.isNull(taskRecord)) {
            //如果找不到任务记录，可能是任务删除了或者是Id为临时记录Id，无任何业务含义
            log.info("can not find record={} maybe is a temp recordId", taskRecordId);
            return false;
        }


        //1 找到任务记录关联的所有用例
        List<FeatureInfoDto> features =
                featureRepository.queryNotContainFolder(taskRecord.getTestCaseId()).stream()
                        //过滤disable的用例
                        .filter(feature -> Objects.equals(feature.getStatus(), FeatureStatus.NORMAL.getType()))
                        .collect(Collectors.toList());
        //2 找到任务关联所有用例的执行记录
        List<FeatureHistoryDto> taskRecordFeatures = featureHistoryRepository.getTaskRecordFeatures(taskRecordId);
        List<String> recordFeatureIds =
                taskRecordFeatures.stream().map(FeatureHistoryDto::getFeatureId).collect(Collectors.toList());
        //3 如果所有用例都有执行记录，那么任务执行完成
        if (Objects.equals(recordFeatureIds.size(), features.size())) {
            ProcessStatus processStatus =
                    taskRecordFeatures.stream().filter(history -> ProcessStatus.exchange(history.getExecuteStatus())
                    .isFailStatus()).findAny().map(f -> ProcessStatus.FAIL).orElse(ProcessStatus.SUCCESS);
            taskRecordRepository.updateRecordStatus(taskRecordId, processStatus.getType());
            taskLogRepository.updateLogStatus(logId, processStatus.getType());
            return true;
        }
        return false;
    }
}
