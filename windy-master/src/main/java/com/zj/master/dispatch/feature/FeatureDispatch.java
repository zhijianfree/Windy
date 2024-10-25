package com.zj.master.dispatch.feature;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.model.DispatchTaskModel;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.TestCaseConfigDto;
import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITestCaseConfigRepository;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.master.dispatch.IDispatchExecutor;
import com.zj.master.dispatch.task.FeatureExecuteProxy;
import com.zj.master.dispatch.task.FeatureTask;
import com.zj.master.entity.vo.ExecuteContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Slf4j
@Component
public class FeatureDispatch implements IDispatchExecutor {

    public static final String TEMP_KEY = "temp_";
    private final IFeatureRepository featureRepository;
    private final ITestCaseConfigRepository testCaseConfigRepository;
    private final UniqueIdService uniqueIdService;
    private final FeatureExecuteProxy featureExecuteProxy;
    private final IDispatchLogRepository dispatchLogRepository;

    public FeatureDispatch(IFeatureRepository featureRepository,
                           ITestCaseConfigRepository testCaseConfigRepository,
                           UniqueIdService uniqueIdService, FeatureExecuteProxy featureExecuteProxy,
                           IDispatchLogRepository dispatchLogRepository) {
        this.featureRepository = featureRepository;
        this.testCaseConfigRepository = testCaseConfigRepository;
        this.uniqueIdService = uniqueIdService;
        this.featureExecuteProxy = featureExecuteProxy;
        this.dispatchLogRepository = dispatchLogRepository;
    }

    @Override
    public LogType type() {
        return LogType.FEATURE;
    }

    @Override
    public boolean isExistInJvm(DispatchLogDto taskLog) {
        return featureExecuteProxy.isExitTask(taskLog.getSourceRecordId());
    }

    @Override
    public String dispatch(DispatchTaskModel task, String logId) {
        String featureString = task.getSourceId();
        List<String> featureIds = JSON.parseArray(featureString, String.class);
        FeatureInfoDto feature = featureRepository.getFeatureById(featureIds.get(0));
        List<TestCaseConfigDto> caseConfigs = testCaseConfigRepository.getCaseConfigs(
                feature.getTestCaseId());
        ExecuteContext executeContext = buildTaskConfig(caseConfigs);
        FeatureTask featureTask = new FeatureTask();
        featureTask.setExecuteContext(executeContext);
        featureTask.addAll(featureIds);
        featureTask.setLogId(logId);

        //这个是临时的recordId，没有任何业务含义，只是为了支持多个用例的执行
        String tempRecordId =
                Optional.ofNullable(task.getTriggerId()).filter(StringUtils::isNotBlank).orElseGet(() -> TEMP_KEY + uniqueIdService.getUniqueId());
        featureTask.setTaskRecordId(tempRecordId);

        featureExecuteProxy.execute(featureTask);
        return tempRecordId;
    }

    @Override
    public boolean resume(DispatchLogDto taskLog) {
        log.info("not support resume temporary feature log={}", taskLog.getLogId());
        dispatchLogRepository.updateLogStatus(taskLog.getLogId(), ProcessStatus.FAIL.getType());
        return false;
    }

    private ExecuteContext buildTaskConfig(List<TestCaseConfigDto> configs) {
        ExecuteContext executeContext = new ExecuteContext();
        if (CollectionUtils.isEmpty(configs)) {
            return executeContext;
        }

        for (TestCaseConfigDto config : configs) {
            executeContext.set(config.getParamKey(), config.getValue());
        }
        return executeContext;
    }

    @Override
    public Integer getExecuteCount() {
        return featureExecuteProxy.getTaskSize();
    }
}
