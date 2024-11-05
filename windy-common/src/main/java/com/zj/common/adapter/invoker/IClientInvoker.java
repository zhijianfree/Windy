package com.zj.common.adapter.invoker;

import com.zj.common.entity.dto.ClientCollectDto;
import com.zj.common.entity.dto.StopDispatch;
import com.zj.common.entity.service.LanguageVersionDto;

import java.util.List;

public interface IClientInvoker {
    boolean runGenerateTask(Object generateInfo);

    boolean runPipelineTask(Object pipelineTask, boolean isRequestSingle, String singleIp);

    boolean runFeatureTask(Object featureTask);

    void stopTaskLoopQuery(StopDispatch stopDispatch);

    List<ClientCollectDto> requestClientMonitor();

    LanguageVersionDto getSupportVersions();
}
