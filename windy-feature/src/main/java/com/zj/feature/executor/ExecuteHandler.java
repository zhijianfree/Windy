package com.zj.feature.executor;

import com.zj.feature.entity.dto.FeatureInfoDTO;
import com.zj.feature.entity.dto.TestCaseConfigDTO;
import com.zj.feature.executor.vo.ExecuteContext;
import com.zj.feature.service.FeatureService;
import com.zj.feature.service.TestCaseConfigService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class ExecuteHandler {

  @Autowired
  private IFeatureExecutor featureExecutor;

  @Autowired
  private FeatureService featureService;


  @Autowired
  private TestCaseConfigService caseConfigService;


  public String executeFeature(String featureId) {
    ExecuteContext executeContext = loadExecuteContext(featureId);
    return featureExecutor.execute(featureId, null, executeContext);
  }


  private ExecuteContext loadExecuteContext(String featureId) {
    FeatureInfoDTO feature = featureService.getFeatureById(featureId);
    String testCaseId = feature.getTestCaseId();
    List<TestCaseConfigDTO> caseConfigs = caseConfigService.getTestCaseConfigs(testCaseId);
    if (CollectionUtils.isEmpty(caseConfigs)) {
      return new ExecuteContext();
    }

    ExecuteContext executeContext = new ExecuteContext();
    caseConfigs.forEach(config -> {
      executeContext.set(config.getParamKey(), config.getValue());
    });
    return executeContext;
  }
}
