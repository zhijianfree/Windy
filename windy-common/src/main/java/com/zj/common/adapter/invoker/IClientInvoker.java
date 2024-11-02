package com.zj.common.adapter.invoker;

import com.zj.common.entity.dto.ClientCollect;
import com.zj.common.entity.dto.StopDispatch;

import java.util.List;

public interface IClientInvoker {
    boolean runGenerateTask(Object generateInfo);

    boolean runPipelineTask(Object pipelineTask, boolean isRequestSingle, String singleIp);

    boolean runFeatureTask(Object featureTask);

    void stopTaskLoopQuery(StopDispatch stopDispatch);

    List<ClientCollect> requestClientMonitor();
}
