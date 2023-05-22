package com.zj.master.dispatch.feature;

import com.alibaba.fastjson.JSON;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.domain.entity.dto.feature.TestCaseConfigDto;
import com.zj.domain.entity.dto.log.TaskLogDto;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITestCaseConfigRepository;
import com.zj.domain.repository.feature.ITestCaseRepository;
import com.zj.master.dispatch.IDispatchExecutor;
import com.zj.master.dispatch.task.FeatureExecuteProxy;
import com.zj.master.dispatch.task.FeatureTask;
import com.zj.master.entity.dto.TaskDetailDto;
import com.zj.common.enums.LogType;
import com.zj.master.entity.vo.ExecuteContext;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Slf4j
@Component
public class FeatureDispatch implements IDispatchExecutor {

  public static final String TEMP_KEY = "temp_";
  @Autowired
  private IFeatureRepository featureRepository;

  @Autowired
  private ITestCaseRepository testCaseRepository;

  @Autowired
  private ITestCaseConfigRepository testCaseConfigRepository;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private FeatureExecuteProxy featureExecuteProxy;

  @Override
  public Integer type() {
    return LogType.FEATURE.getType();
  }

  @Override
  public boolean dispatch(TaskDetailDto task) {
    String featureString = task.getSourceId();
    List<String> featureIds = JSON.parseArray(featureString, String.class);
    FeatureInfoDto feature = featureRepository.getFeatureById(featureIds.get(0));
    List<TestCaseConfigDto> caseConfigs = testCaseConfigRepository.getCaseConfigs(
        feature.getTestCaseId());
    ExecuteContext executeContext = buildTaskConfig(caseConfigs);
    FeatureTask featureTask = new FeatureTask();
    featureTask.setExecuteContext(executeContext);
    featureTask.addAll(featureIds);
    featureTask.setLogId(task.getTaskLogId());

    //这个是临时的recordId，没有任何业务含义，只是为了支持多个用例的执行
    String tempRecordId = TEMP_KEY + uniqueIdService.getUniqueId();
    featureTask.setTaskRecordId(tempRecordId);

    featureExecuteProxy.execute(featureTask);
    return false;
  }

  @Override
  public boolean resume(TaskLogDto taskLog) {
    log.info("临时任务不支持恢复");
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
}
