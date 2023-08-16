package com.zj.master.dispatch.pipeline;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.model.StopDispatch;
import com.zj.common.monitor.RequestProxy;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.master.dispatch.listener.IStopEventListener;
import com.zj.master.dispatch.listener.InnerEvent;
import com.zj.master.dispatch.pipeline.intercept.INodeExecuteInterceptor;
import com.zj.master.entity.vo.NodeStatusChange;
import com.zj.master.entity.vo.RequestContext;
import com.zj.master.entity.vo.TaskNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
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
  public static final String DISPATCH_PIPELINE_TYPE = "PIPELINE";

  private final RequestProxy requestProxy;
  private final Executor executorService;
  private final INodeRecordRepository nodeRecordRepository;
  private final IPipelineHistoryRepository pipelineHistoryRepository;
  private final PipelineEndProcessor pipelineEndProcessor;
  private final List<INodeExecuteInterceptor> interceptors;

  private final Map<String, PipelineTask> pipelineTaskMap = new ConcurrentHashMap<>();

  public PipelineExecuteProxy(RequestProxy requestProxy,
      @Qualifier("pipelineExecutorPool") Executor executorService,
      INodeRecordRepository nodeRecordRepository,
      IPipelineHistoryRepository pipelineHistoryRepository,
      PipelineEndProcessor pipelineEndProcessor, List<INodeExecuteInterceptor> interceptors) {
    this.requestProxy = requestProxy;
    this.executorService = executorService;
    this.nodeRecordRepository = nodeRecordRepository;
    this.pipelineHistoryRepository = pipelineHistoryRepository;
    this.pipelineEndProcessor = pipelineEndProcessor;
    this.interceptors = interceptors.stream()
        .sorted(Comparator.comparing(INodeExecuteInterceptor::sort)).collect(Collectors.toList());
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
      taskNode.setDispatchType(DISPATCH_PIPELINE_TYPE);
      taskNode.setMasterIp(IpUtils.getLocalIP());

      interceptBefore(taskNode);

      RequestContext requestContext = taskNode.getRequestContext();
      boolean dispatchResult = requestProxy.sendDispatchTask(taskNode,
          requestContext.isRequestSingle(), requestContext.getSingleClientIp());
      if (!dispatchResult) {
        log.info("dispatch pipeline task to client fail logId={}", logId);
        NodeStatusChange change = buildStatusChange(pipelineTask, taskNode.getHistoryId(),
            taskNode.getNodeId(), ProcessStatus.FAIL);
        pipelineEndProcessor.statusChange(change);
        return null;
      }
      return taskNode;
    }, executorService).whenComplete((node, e) -> {
      String recordId = Optional.ofNullable(node).map(TaskNode::getRecordId).orElse(TASK_DONE_TIPS);
      log.info("complete trigger action recordId = {}", recordId);
    }).exceptionally((e) -> {
      log.error("handle task error", e);
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
      }catch (Exception e){
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
    //todo 如果节点配置跳过，则修改状态为IGNORE
//    if (processStatus.isFailStatus() && taskNode.getNodeConfig().isIgnoreError()) {
//      processStatus = ProcessStatus.IGNORE_FAIL;
//    }

    //1 获取流水线关联的任务
    PipelineTask pipelineTask = pipelineTaskMap.get(nodeRecord.getHistoryId());
    if (Objects.isNull(pipelineTask)) {
      log.info("not find Pipeline task historyId={}", nodeRecord.getHistoryId());
      return;
    }

    //2 节点执行完成是否触发整个流水线结束
    NodeRecordDto record = nodeRecordRepository.getRecordById(nodeRecord.getRecordId());
    NodeStatusChange statusChange = buildStatusChange(pipelineTask, nodeRecord.getHistoryId(),
        record.getNodeId(), ProcessStatus.exchange(nodeRecord.getStatus()));
    pipelineEndProcessor.statusChange(statusChange);

    //3 继续递归执行下一个任务
    runTaskNodeFromPipeline(pipelineTask);
  }

  @Override
  @Subscribe
  @AllowConcurrentEvents
  public void stopEvent(InnerEvent event) {
    if (!Objects.equals(event.getLogType().getType(), LogType.PIPELINE.getType())) {
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
    requestProxy.stopDispatchTask(stopDispatch);
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
