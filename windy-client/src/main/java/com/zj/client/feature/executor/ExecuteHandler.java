package com.zj.client.feature.executor;

import com.zj.client.entity.po.ExecutePoint;
import com.zj.client.feature.executor.feature.IFeatureExecutor;
import com.zj.client.feature.executor.vo.ExecuteContext;
import com.zj.client.feature.executor.vo.ExecuteResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExecuteHandler {

  @Autowired
  private IFeatureExecutor featureExecutor;

  public CompletableFuture<ExecuteResult> executeFeature(List<ExecutePoint> executePointList, String featureId, String recordId,
      ExecuteContext executeContext) {
    return featureExecutor.execute(executePointList, featureId, recordId, executeContext);
  }
}
