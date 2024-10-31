package com.zj.common.monitor.invoker;

import com.zj.common.model.ClientCollect;
import com.zj.common.model.StopDispatch;

import java.util.List;

public interface IClientInvoker {
    boolean runGenerateTask(Object generateInfo);

    boolean runPipelineTask(Object pipelineTask);

    boolean runFeatureTask(Object featureTask, boolean isRequestSingle, String singleIp);

    void stopTaskLoopQuery(StopDispatch stopDispatch);

    List<ClientCollect> requestClientMonitor();
}
