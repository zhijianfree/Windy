package com.zj.master.schedule;

import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.domain.repository.feature.IExecuteRecordRepository;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.domain.repository.pipeline.IOptimisticLockRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class HistoryDataCleanSchedule {

    @Value("${windy.clean.feature}")
    private Integer cleanFeatureTime;

    @Value("${windy.clean.pipeline}")
    private Integer cleanPipelineTime;
    @Value("${windy.clean.task}")
    private Integer cleanTaskTime;

    private final String PIPELINE_HISTORY_DATA_CLEAN = "pipeline_history_data_clean";
    private final String FEATURE_HISTORY_DATA_CLEAN = "feature_history_data_clean";
    private final String TASK_HISTORY_DATA_CLEAN = "task_history_data_clean";

    private final IOptimisticLockRepository optimisticLockRepository;
    private final IPipelineHistoryRepository pipelineHistoryRepository;
    private final INodeRecordRepository nodeRecordRepository;
    private final IFeatureHistoryRepository featureHistoryRepository;
    private final IExecuteRecordRepository executeRecordRepository;
    private final ITaskRecordRepository taskRecordRepository;

    public HistoryDataCleanSchedule(IOptimisticLockRepository optimisticLockRepository, IPipelineHistoryRepository pipelineHistoryRepository,
                                    INodeRecordRepository nodeRecordRepository,
                                    IFeatureHistoryRepository featureHistoryRepository,
                                    IExecuteRecordRepository executeRecordRepository, ITaskRecordRepository taskRecordRepository) {
        this.optimisticLockRepository = optimisticLockRepository;
        this.pipelineHistoryRepository = pipelineHistoryRepository;
        this.nodeRecordRepository = nodeRecordRepository;
        this.featureHistoryRepository = featureHistoryRepository;
        this.executeRecordRepository = executeRecordRepository;
        this.taskRecordRepository = taskRecordRepository;
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void cleanPipelineHistory() {
        log.info("start scan clean pipeline history");
        boolean tryLock = optimisticLockRepository.tryLock(PIPELINE_HISTORY_DATA_CLEAN);
        if (!tryLock){
            return;
        }
        log.info("start clean pipeline history");
        long oldTime = new DateTime().minusHours(cleanPipelineTime).getMillis();
        List<PipelineHistoryBO> pipelineHistories = pipelineHistoryRepository.getOldPipelineHistory(oldTime);
        pipelineHistories.forEach(this::deletePipelineHistory);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanTaskHistory() {
        log.info("start scan clean task history");
        boolean tryLock = optimisticLockRepository.tryLock(TASK_HISTORY_DATA_CLEAN);
        if (!tryLock){
            return;
        }
        log.info("start clean task record");
        long oldTime = new DateTime().minusHours(cleanTaskTime).getMillis();
        List<TaskRecordBO> pipelineHistories = taskRecordRepository.getOldTaskRecord(oldTime);
        pipelineHistories.forEach(taskRecord -> {
            try {
                taskRecordRepository.deleteTaskRecord(taskRecord.getRecordId());
            }catch (Exception e){
                log.info("delete task record error", e);
            }
        });
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanFeatureHistory() {
        log.info("start scan clean feature history");
        boolean tryLock = optimisticLockRepository.tryLock(FEATURE_HISTORY_DATA_CLEAN);
        if (!tryLock){
            return;
        }

        log.info("start clean feature history");
        long featureOldTime = new DateTime().minusHours(cleanFeatureTime).getMillis();
        List<FeatureHistoryBO> featureHistories = featureHistoryRepository.getOldFeatureHistory(featureOldTime);
        featureHistories.forEach(this::deleteFeatureHistories);
    }

    private void deletePipelineHistory(PipelineHistoryBO pipelineHistory) {
        try {
            boolean deleteRecord = nodeRecordRepository.deleteRecordByHistoryId(pipelineHistory.getHistoryId());
            boolean deleteHistory = pipelineHistoryRepository.deleteByHistoryId(pipelineHistory.getHistoryId());
            log.info("delete pipeline history result deleteHistory={} deleteRecord={}", deleteHistory, deleteRecord);
        } catch (Exception e) {
            log.info("delete pipeline history error", e);
        }
    }

    private void deleteFeatureHistories(FeatureHistoryBO featureHistoryBO) {
        boolean deleteRecord = executeRecordRepository.deleteByHistoryId(featureHistoryBO.getHistoryId());
        boolean deleteHistory = featureHistoryRepository.deleteHistory(featureHistoryBO.getHistoryId());
        log.info("delete feature history result deleteHistory={} deleteRecord={}", deleteHistory, deleteRecord);
    }
}
