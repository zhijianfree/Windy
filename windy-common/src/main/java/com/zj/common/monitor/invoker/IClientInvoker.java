package com.zj.common.monitor.invoker;

import com.zj.common.model.ClientCollect;
import com.zj.common.model.StopDispatch;

import java.util.List;

public interface IClientInvoker {
    boolean runGenerateTask(Object generateInfo);

    boolean runPipelineTask(Object pipelineTask, boolean isRequestSingle, String singleIp);

    boolean runFeatureTask(Object featureTask);

    void stopTaskLoopQuery(StopDispatch stopDispatch);

    List<ClientCollect> requestClientMonitor();
}
