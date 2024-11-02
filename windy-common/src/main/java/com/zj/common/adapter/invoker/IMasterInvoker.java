package com.zj.common.adapter.invoker;

import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.common.entity.dto.MasterCollect;
import com.zj.common.entity.dto.PluginInfo;
import com.zj.common.entity.dto.ResponseStatusModel;
import com.zj.common.entity.dto.ResultEvent;

import java.util.List;

public interface IMasterInvoker {

    /**
     * 执行用例任务
     */
    String runFeatureTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 执行生成二方包任务
     */
    Boolean runGenerateTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 执行流水线任务
     */
    String startPipelineTask(DispatchTaskModel dispatchTaskModel);

    /**
     * 获取用例任务执行状态
     */
    ResponseStatusModel getFeatureTaskStatus(String taskRecordId);

    boolean notifyExecuteEvent(ResultEvent resultEvent);

    ResponseStatusModel getApprovalRecord(String recordId);

    List<PluginInfo> getAvailablePlugins();

    boolean stopDispatchTask(DispatchTaskModel dispatchTaskModel);

    List<MasterCollect> requestMasterMonitor();
}
