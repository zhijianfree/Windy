package com.zj.master.notify.event;

import com.alibaba.fastjson.JSON;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.enums.TemplateType;
import com.zj.domain.entity.bo.feature.ExecuteRecordBO;
import com.zj.domain.entity.bo.feature.FeatureHistoryBO;
import com.zj.domain.repository.feature.IExecuteRecordRepository;
import com.zj.domain.repository.feature.IFeatureHistoryRepository;
import com.zj.master.notify.INotifyEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Component
public class UpdatePointRecordNotifyEvent implements INotifyEvent {

    private final IExecuteRecordRepository executeRecordRepository;
    private final IFeatureHistoryRepository historyRepository;

    public UpdatePointRecordNotifyEvent(IExecuteRecordRepository executeRecordRepository,
                                        IFeatureHistoryRepository historyRepository) {
        this.executeRecordRepository = executeRecordRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public NotifyType type() {
        return NotifyType.UPDATE_EXECUTE_POINT_RECORD;
    }

    @Override
    public boolean handle(ResultEvent resultEvent) {
        log.info("receive execute record update event id = {} event={}", resultEvent.getExecuteId(),
                JSON.toJSONString(resultEvent.getParams()));
        ExecuteRecordBO executeRecordBO = JSON.parseObject(JSON.toJSONString(resultEvent.getParams()), ExecuteRecordBO.class);
        updateFeatureHistory(executeRecordBO);
        return executeRecordRepository.updateStatusAndResult(executeRecordBO);
    }

    private void updateFeatureHistory(ExecuteRecordBO executeRecordBO) {
        if (!Objects.equals(TemplateType.THREAD.getType(), executeRecordBO.getExecuteType())) {
            return;
        }

        //如果用例状态已经是失败状态则不需要处理
        String historyId = executeRecordBO.getHistoryId();
        FeatureHistoryBO featureHistory = historyRepository.getFeatureHistory(historyId);
        if (ProcessStatus.exchange(featureHistory.getExecuteStatus()).isFailStatus()) {
            return;
        }

        // 当前执行点执行的结果为失败状态时才需更新用例状态
        if (ProcessStatus.exchange(executeRecordBO.getStatus()).isFailStatus() ||
                Objects.equals(featureHistory.getExecuteStatus(), ProcessStatus.RUNNING.getType())) {
            featureHistory.setExecuteStatus(executeRecordBO.getStatus());
            boolean update = historyRepository.updateStatus(executeRecordBO.getHistoryId(),
                    executeRecordBO.getStatus());
            log.info("async execute record update feature history status = {} recordId={} historyId={} result={}",
                    executeRecordBO.getStatus(), executeRecordBO.getExecuteRecordId(), historyId, update);
        }
    }
}
