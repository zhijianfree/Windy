package com.zj.master.dispatch.task;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.FeatureStatus;
import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.master.entity.vo.BatchFeatureVo;
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
        TaskRecordBO taskRecord = taskRecordRepository.getTaskRecord(taskRecordId);
        if (Objects.isNull(taskRecord)) {
            //如果找不到任务记录，可能是任务删除了或者是Id为临时记录Id，无任何业务含义
            log.info("can not find record={} maybe is a temp recordId", taskRecordId);
            return false;
        }

        //1 找到任务记录关联的所有用例
        List<String> featureIds =
                featureRepository.queryNotContainFolder(taskRecord.getTestCaseId()).stream()
                        //过滤disable的用例
                        .filter(feature -> Objects.equals(feature.getStatus(), FeatureStatus.NORMAL.getType()))
                        .map(FeatureInfoBO::getFeatureId)
                        .collect(Collectors.toList());
        if (Objects.equals(taskRecord.getTestCaseId(), taskRecord.getTriggerId())) {
            //如果测试集ID与触发源Id一致证明是临时批量执行任务，需要根据任务选择的用例个数来判断最终状态
            String taskConfig = taskRecord.getTaskConfig();
            BatchFeatureVo batchFeatureVo = JSON.parseObject(taskConfig, BatchFeatureVo.class);
            featureIds = batchFeatureVo.getFeatureIds();
        }

        //2 找到任务关联所有用例的执行记录
        List<FeatureHistoryBO> taskRecordFeatures = featureHistoryRepository.getHistoriesByTaskRecordId(taskRecordId);
        List<String> recordFeatureIds =
                taskRecordFeatures.stream().map(FeatureHistoryBO::getFeatureId).collect(Collectors.toList());
        //3 如果所有用例都有执行记录，那么任务执行完成
        if (Objects.equals(recordFeatureIds.size(), featureIds.size())) {
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
