package com.zj.feature.executor;

import com.zj.feature.entity.dto.TaskRecordDTO;
import com.zj.feature.executor.vo.ExecuteContext;
import java.util.List;

public interface IFeatureExecutor {

    String execute(String featureId, String recordId, ExecuteContext executeContext);

    List<String> batchRunTask(List<String> featureIds, TaskRecordDTO taskRecord);
}
