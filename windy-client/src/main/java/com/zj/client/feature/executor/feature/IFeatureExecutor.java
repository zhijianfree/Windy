package com.zj.client.feature.executor.feature;

import com.zj.client.entity.po.ExecutePoint;
import com.zj.client.feature.executor.vo.ExecuteContext;
import com.zj.client.feature.executor.vo.ExecuteResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IFeatureExecutor {

    void execute(List<ExecutePoint> executePointList, String featureId, String recordId,
        ExecuteContext executeContext);
}
