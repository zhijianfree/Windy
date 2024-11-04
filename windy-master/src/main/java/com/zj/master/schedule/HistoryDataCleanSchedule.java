package com.zj.master.schedule;

import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.entity.bo.pipeline.PipelineHistoryBO;
import com.zj.domain.repository.feature.IExecuteRecordRepository;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
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
    private final IPipelineHistoryRepository pipelineHistoryRepository;
    private final INodeRecordRepository nodeRecordRepository;
    private final IFeatureHistoryRepository featureHistoryRepository;
    private final IExecuteRecordRepository executeRecordRepository;

    public HistoryDataCleanSchedule(IPipelineHistoryRepository pipelineHistoryRepository,
                                    INodeRecordRepository nodeRecordRepository,
                                    IFeatureHistoryRepository featureHistoryRepository,
                                    IExecuteRecordRepository executeRecordRepository) {
        this.pipelineHistoryRepository = pipelineHistoryRepository;
        this.nodeRecordRepository = nodeRecordRepository;
        this.featureHistoryRepository = featureHistoryRepository;
        this.executeRecordRepository = executeRecordRepository;
    }

    //    @Scheduled(cron = "0 0 23 * * ?")
    @Scheduled(cron = "0/5 * * * * ? ")
    public void cleanPipelineHistory() {
        log.info("start scan clean pipeline history");
        long oldTime = new DateTime().minusHours(cleanPipelineTime).getMillis();
        List<PipelineHistoryBO> pipelineHistories = pipelineHistoryRepository.getOldPipelineHistory(oldTime);
        pipelineHistories.forEach(this::deletePipelineHistory);
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

    //    @Scheduled(cron = "0 0 1 * * ?")
    @Scheduled(cron = "0/5 * * * * ? ")
    public void cleanFeatureHistory() {
        log.info("start scan clean feature history");
        long featureOldTime = new DateTime().minusHours(cleanFeatureTime).getMillis();
        List<FeatureHistoryBO> featureHistories = featureHistoryRepository.getOldFeatureHistory(featureOldTime);
        featureHistories.forEach(this::deleteFeatureHistories);
    }

    private void deleteFeatureHistories(FeatureHistoryBO featureHistoryBO) {
        boolean deleteRecord = executeRecordRepository.deleteByHistoryId(featureHistoryBO.getHistoryId());
        boolean deleteHistory = featureHistoryRepository.deleteHistory(featureHistoryBO.getHistoryId());
        log.info("delete feature history result deleteHistory={} deleteRecord={}", deleteHistory, deleteRecord);
    }
}
