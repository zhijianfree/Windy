package com.zj.master.dispatch.pipeline;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.zj.common.enums.DispatchType;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.model.StopDispatch;
import com.zj.common.monitor.invoker.IClientInvoker;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.master.dispatch.listener.IStopEventListener;
import com.zj.master.dispatch.listener.InternalEvent;
import com.zj.master.dispatch.pipeline.intercept.INodeExecuteInterceptor;
import com.zj.master.entity.vo.NodeStatusChange;
import com.zj.master.entity.vo.RequestContext;
import com.zj.master.entity.vo.TaskNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@Slf4j
@Component
public class PipelineExecuteProxy implements IStopEventListener {

    public static final String TASK_DONE_TIPS = "no task need run";
    private final Executor executorService;
    private final INodeRecordRepository nodeRecordRepository;
    private final IPipelineHistoryRepository pipelineHistoryRepository;
    private final PipelineEndProcessor pipelineEndProcessor;
    private final List<INodeExecuteInterceptor> interceptors;
    private final IClientInvoker clientInvoker;

    private final Map<String, PipelineTask> pipelineTaskMap = new ConcurrentHashMap<>();

    public PipelineExecuteProxy(@Qualifier("pipelineExecutorPool") Executor executorService,
                                INodeRecordRepository nodeRecordRepository,
                                IPipelineHistoryRepository pipelineHistoryRepository,
                                PipelineEndProcessor pipelineEndProcessor, List<INodeExecuteInterceptor> interceptors
            , IClientInvoker clientInvoker) {
        this.executorService = executorService;
        this.nodeRecordRepository = nodeRecordRepository;
        this.pipelineHistoryRepository = pipelineHistoryRepository;
        this.pipelineEndProcessor = pipelineEndProcessor;
        this.interceptors = interceptors.stream()
                .sorted(Comparator.comparing(INodeExecuteInterceptor::sort)).collect(Collectors.toList());
        this.clientInvoker = clientInvoker;
    }

    public void runTask(PipelineTask pipelineTask) {
        log.info("start run task ={}", JSON.toJSONString(pipelineTask));
        pipelineTaskMap.put(pipelineTask.getHistoryId(), pipelineTask);
        runTaskNodeFromPipeline(pipelineTask);
    }

    private void runTaskNodeFromPipeline(PipelineTask pipelineTask) {
        TaskNode taskNode = pollAndCheckTask(pipelineTask);
        if (Objects.isNull(taskNode)) {
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            String logId = pipelineTask.getLogId();
            taskNode.setLogId(logId);
            taskNode.setDispatchType(DispatchType.PIPELINE.name());
            taskNode.setMasterIp(IpUtils.getLocalIP());

            interceptBefore(taskNode);

            RequestContext requestContext = taskNode.getRequestContext();
            boolean dispatchResult = clientInvoker.runPipelineTask(taskNode, requestContext.isRequestSingle(),
                    requestContext.getSingleClientIp());
            if (!dispatchResult) {
                log.info("dispatch pipeline task to client fail logId={}", logId);
                //todo 这个地方需要将错误描述添加进来，否则控制台不知道什原因
                NodeStatusChange change = buildStatusChange(pipelineTask, taskNode.getHistoryId(),
                        taskNode.getNodeId(), ProcessStatus.FAIL);
                pipelineEndProcessor.statusChange(change);
                return null;
            }
            return taskNode;
        }, executorService).whenComplete((node, e) -> {
            String recordId = Optional.ofNullable(node).map(TaskNode::getRecordId).orElse(TASK_DONE_TIPS);
            log.info("complete trigger action recordId = {}", recordId);
        }).exceptionally(e -> {
            log.error("handle task error", e);
            //todo 这个地方需要将错误描述添加进来，否则控制台不知道什原因
            NodeStatusChange change = buildStatusChange(pipelineTask, taskNode.getHistoryId(),
                    taskNode.getNodeId(), ProcessStatus.FAIL);
            pipelineEndProcessor.statusChange(change);
            return null;
        });
    }

    private void interceptBefore(TaskNode taskNode) {
        interceptors.forEach(interceptor -> {
            try {
                interceptor.beforeExecute(taskNode);
            } catch (Exception e) {
                log.error("intercept before", e);
            }
        });
    }


    private TaskNode pollAndCheckTask(PipelineTask pipelineTask) {
        LinkedBlockingQueue<TaskNode> taskNodeQueue = pipelineTask.getTaskNodes();
        TaskNode taskNode = taskNodeQueue.poll();
        if (Objects.isNull(taskNode)) {
            log.info("can not find pipeline task node");
            return null;
        }

        PipelineHistoryDto pipelineHistory = pipelineHistoryRepository.getPipelineHistory(
                taskNode.getHistoryId());
        if (Objects.isNull(pipelineHistory) || ProcessStatus.isCompleteStatus(
                pipelineHistory.getPipelineStatus())) {
            log.info("can not find pipeline history or history has done. historyId={}",
                    taskNode.getHistoryId());
            return null;
        }
        return taskNode;
    }


    public void statusChange(NodeRecordDto nodeRecord) {
        //1 获取流水线关联的任务
        PipelineTask pipelineTask = pipelineTaskMap.get(nodeRecord.getHistoryId());
        if (Objects.isNull(pipelineTask)) {
            log.info("not find Pipeline task historyId={}", nodeRecord.getHistoryId());
            return;
        }

        //2 节点执行完成是否触发整个流水线结束
        NodeRecordDto nodeRecordDto = nodeRecordRepository.getRecordById(nodeRecord.getRecordId());
        NodeStatusChange statusChange = buildStatusChange(pipelineTask, nodeRecord.getHistoryId(),
                nodeRecordDto.getNodeId(), ProcessStatus.exchange(nodeRecord.getStatus()));
        pipelineEndProcessor.statusChange(statusChange);

        //3 继续递归执行下一个任务
        if (ProcessStatus.isCompleteStatus(nodeRecord.getStatus())) {
            runTaskNodeFromPipeline(pipelineTask);
        }
    }

    @Override
    @Subscribe
    @AllowConcurrentEvents
    public void stopEvent(InternalEvent event) {
        if (Objects.isNull(event.getLogType()) || !Objects.equals(event.getLogType().getType(),
                LogType.PIPELINE.getType())) {
            return;
        }

        //如果是判断是否当前实例在执行流水线任务
        String historyId = event.getTargetId();
        PipelineTask pipelineTask = pipelineTaskMap.get(historyId);
        if (Objects.nonNull(pipelineTask)) {
            pipelineTaskMap.remove(historyId);
        }

        pipelineHistoryRepository.updateStatus(historyId, ProcessStatus.STOP);
        nodeRecordRepository.updateRunningNodeStatus(historyId, ProcessStatus.STOP);

        //只有流水线的执行才需要通知到client
        StopDispatch stopDispatch = new StopDispatch();
        stopDispatch.setLogType(event.getLogType());
        stopDispatch.setTargetId(historyId);
        clientInvoker.stopTaskLoopQuery(stopDispatch);
        log.info("stop pipeline task historyId={}", historyId);
    }

    public boolean isExitTask(String sourceRecordId) {
        return pipelineTaskMap.containsKey(sourceRecordId);
    }

    private NodeStatusChange buildStatusChange(PipelineTask pipelineTask, String historyId,
                                               String nodeId, ProcessStatus status) {
        return NodeStatusChange.builder().historyId(historyId).nodeId(nodeId).processStatus(status)
                .logId(pipelineTask.getLogId()).pipelineId(pipelineTask.getPipelineId()).build();
    }

    public Integer getTaskSize() {
        return pipelineTaskMap.values().stream().mapToInt(task -> task.getTaskNodes().size()).sum();
    }
}
