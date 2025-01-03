package com.zj.client.handler.pipeline.executer;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.zj.client.entity.bo.NodeRecord;
import com.zj.client.handler.notify.IResultEventNotify;
import com.zj.client.handler.pipeline.executer.notify.IPipelineStatusListener;
import com.zj.client.handler.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.IpUtils;
import com.zj.common.utils.TraceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@Slf4j
@Component
public class ExecuteProxy implements IPipelineStatusListener {

    private final NodeExecutor nodeExecutor;
    private final IResultEventNotify resultEventNotify;
    private final Executor executorService;

    public ExecuteProxy(NodeExecutor nodeExecutor, IResultEventNotify resultEventNotify,
                        @Qualifier("pipelinePool") Executor executorService) {
        this.nodeExecutor = nodeExecutor;
        this.resultEventNotify = resultEventNotify;
        this.executorService = executorService;
    }

    /**
     * 流水线的执行应该是每个节点做为一个任务，这样就可以充分使用client的扩展性
     */
    public void runNode(TaskNode taskNode) {
        String traceId = TraceUtils.getTraceId();
        CompletableFuture.supplyAsync(() -> {
            if (Objects.isNull(taskNode)) {
                return null;
            }
            nodeExecutor.runNodeTask(taskNode.getHistoryId(), taskNode);
            return taskNode.getRecordId();
        }, executorService).whenComplete((node, e) -> log.info("complete trigger action recordId = {} traceId={}",
                JSON.toJSONString(node), traceId));
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void statusChange(PipelineStatusEvent event) {
        log.info("receive event bus notify node={} recordId={} processStatus={} ", event.getTaskNode().getNodeId(),
                event.getTaskNode().getRecordId(), event.getProcessStatus());
        TaskNode taskNode = event.getTaskNode();
        //如果节点配置跳过，则修改状态为IGNORE
        ProcessStatus processStatus = event.getProcessStatus();
        if (processStatus.isFailStatus() && taskNode.getNodeConfig().isIgnoreError()) {
            processStatus = ProcessStatus.IGNORE_FAIL;
        }

        NodeRecord nodeRecord = new NodeRecord();
        nodeRecord.setRecordId(taskNode.getRecordId());
        nodeRecord.setNodeId(taskNode.getNodeId());
        nodeRecord.setHistoryId(taskNode.getHistoryId());
        nodeRecord.setResult(event.getErrorMsg());

        ResultEvent resultEvent = new ResultEvent().executeId(taskNode.getRecordId())
                .notifyType(NotifyType.UPDATE_NODE_RECORD)
                .status(processStatus)
                .logId(taskNode.getLogId())
                .executeType(taskNode.getExecuteType())
                .params(nodeRecord)
                .clientIp(IpUtils.getLocalIP())
                .masterIP(taskNode.getMasterIp())
                .context(event.getContext());
        resultEventNotify.notifyEvent(resultEvent);
    }
}
