package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.adapter.invoker.IMasterInvoker;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.feature.ExecutePointBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.bo.feature.TaskRecordBO;
import com.zj.domain.entity.bo.feature.TestCaseBO;
import com.zj.domain.entity.enums.TaskRecordType;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITaskRecordRepository;
import com.zj.domain.repository.feature.ITestCaseRepository;
import com.zj.feature.entity.BatchExecuteFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2022/12/12
 */
@Slf4j
@Service
public class TestCaseService {
    private final UniqueIdService uniqueIdService;
    private final ITestCaseRepository testCaseRepository;
    private final IFeatureRepository featureRepository;
    private final ITaskRecordRepository taskRecordRepository;
    private final IAuthService authService;
    private final IMasterInvoker masterInvoker;

    public TestCaseService(UniqueIdService uniqueIdService, ITestCaseRepository testCaseRepository,
                           IFeatureRepository featureRepository, ITaskRecordRepository taskRecordRepository,
                           IAuthService authService, IMasterInvoker masterInvoker) {
        this.uniqueIdService = uniqueIdService;
        this.testCaseRepository = testCaseRepository;
        this.featureRepository = featureRepository;
        this.taskRecordRepository = taskRecordRepository;
        this.authService = authService;
        this.masterInvoker = masterInvoker;
    }

    public PageSize<TestCaseBO> getE2ECases(Integer page, Integer pageSize) {
        IPage<TestCaseBO> pageObj = testCaseRepository.getE2ECasesPage(page, pageSize);
        return convertPageSize(pageObj);
    }

    public PageSize<TestCaseBO> convertPageSize(IPage<TestCaseBO> page) {
        List<TestCaseBO> records = page.getRecords();
        PageSize<TestCaseBO> dtoPageSize = new PageSize<>();
        if (CollectionUtils.isEmpty(records)) {
            dtoPageSize.setTotal(0);
            return dtoPageSize;
        }


        long total = page.getTotal();
        dtoPageSize.setTotal(total);
        dtoPageSize.setData(records);
        return dtoPageSize;
    }

    public PageSize<TestCaseBO> getTestCaseList(String serviceId, Integer page, Integer pageSize) {
        IPage<TestCaseBO> pageObj = testCaseRepository.getCasePage(serviceId, page, pageSize);
        return convertPageSize(pageObj);
    }

    public String createTestCase(TestCaseBO testCaseBO) {
        String testCaseId = uniqueIdService.getUniqueId();
        testCaseBO.setTestCaseId(testCaseId);
        boolean saveCase = testCaseRepository.saveCase(testCaseBO);
        return saveCase ? testCaseId : "";
    }

    public Boolean updateTestCase(TestCaseBO testCaseBO) {
        long dateNow = System.currentTimeMillis();
        testCaseBO.setUpdateTime(dateNow);
        return testCaseRepository.updateCase(testCaseBO);
    }

    public TestCaseBO getTestCase(String caseId) {
        return testCaseRepository.getTestCaseById(caseId);
    }

    public Boolean deleteTestCase(String caseId) {
        List<FeatureInfoBO> featureList = featureRepository.queryFeatureList(caseId);
        if (CollectionUtils.isNotEmpty(featureList)) {
            log.info("there are features in the case , can not delete caseId={}", caseId);
            throw new ApiException(ErrorCode.CASE_EXIST_FEATURE);
        }
        return testCaseRepository.deleteTestCase(caseId);
    }

    public List<TestCaseBO> getE2ECases() {
        return testCaseRepository.getE2ECases();
    }

    public boolean executeFeature(String caseId, BatchExecuteFeature batchExecute) {
        String taskName = "批量执行 " + authService.getUserDetail().getNickName();
        TaskRecordBO taskRecord = createTempTaskRecord(caseId, taskName, batchExecute);
        boolean createRecord = taskRecordRepository.save(taskRecord);
        if (!createRecord) {
            log.info("create record error, can not execute batch features caseId={}", caseId);
            return false;
        }

        DispatchTaskModel dispatchTaskModel = new DispatchTaskModel();
        dispatchTaskModel.setType(LogType.FEATURE.getType());
        dispatchTaskModel.setSourceId(JSON.toJSONString(batchExecute.getFeatureIds()));
        dispatchTaskModel.setSourceName(taskName);
        dispatchTaskModel.setTriggerId(taskRecord.getRecordId());
        String recordId = masterInvoker.runFeatureTask(dispatchTaskModel);
        return Objects.nonNull(recordId);
    }

    private TaskRecordBO createTempTaskRecord(String caseId, String taskName, BatchExecuteFeature batchExecute) {
        TaskRecordBO taskRecord = new TaskRecordBO();
        taskRecord.setRecordId(uniqueIdService.getUniqueId());
        taskRecord.setTriggerId(caseId);
        taskRecord.setType(TaskRecordType.TEMP_TASK.getType());
        taskRecord.setTaskConfig(JSON.toJSONString(batchExecute));
        taskRecord.setTestCaseId(caseId);
        taskRecord.setStatus(ProcessStatus.RUNNING.getType());
        taskRecord.setTaskName(taskName);
        return taskRecord;
    }
}
