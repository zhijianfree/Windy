package com.zj.feature.executor;

import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.feature.executor.vo.ExecuteContext;
import java.util.List;

public interface IFeatureExecutor {

    String execute(String featureId, String recordId, ExecuteContext executeContext);

    List<String> batchRunTask(List<String> featureIds, TaskRecordDto taskRecord);
}
