package com.zj.master.dispatch.pipeline;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.Subscribe;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.model.StopDispatch;
import com.zj.common.monitor.RequestProxy;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.master.dispatch.listener.IStopEventListener;
import com.zj.master.dispatch.listener.InnerEvent;
import com.zj.master.dispatch.pipeline.intercept.INodeExecuteInterceptor;
import com.zj.master.entity.vo.TaskNode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@Slf4j
@Component
public class PipelineExecuteProxy implements IStopEventListener {

  public static final String TASK_DONE_TIPS = "no task need run";
  public static final String DISPATCH_PIPELINE_TYPE = "PIPELINE";
  @Autowired
  private RequestProxy requestProxy;
  @Autowired
  @Qualifier("pipelineExecutorPool")
  private ExecutorService executorService;
  @Autowired
  private INodeRecordRepository nodeRecordRepository;
  @Autowired
  private IPipelineHistoryRepository pipelineHistoryRepository;
  @Autowired
  private PipelineEndProcessor pipelineEndProcessor;
  @Autowired
  private IDispatchLogRepository dispatchLogRepository;
  @Autowired
  private List<INodeExecuteInterceptor> interceptors;

  private final Map<String, PipelineTask> pipelineTaskMap = new ConcurrentHashMap<>();

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
      boolean dispatchResult = requestProxy.sendDispatchTask(taskNode);
      if (!dispatchResult) {
        log.info("dispatch pipeline task to client fail ");
        pipelineHistoryRepository.updateStatus(taskNode.getHistoryId(), ProcessStatus.FAIL);
        dispatchLogRepository.updateLogStatus(logId, ProcessStatus.FAIL.getType());
        return null;
      }
      return taskNode;
    }, executorService).whenComplete((node, e) -> {
      String recordId = Optional.ofNullable(node).map(TaskNode::getRecordId).orElse(TASK_DONE_TIPS);
      log.info("complete trigger action recordId = {}", recordId);
    }).exceptionally((e) -> {
      log.error("handle task error", e);
      pipelineHistoryRepository.updateStatus(taskNode.getHistoryId(), ProcessStatus.FAIL);
      dispatchLogRepository.updateLogStatus(pipelineTask.getLogId(), ProcessStatus.FAIL.getType());
      return null;
    });
  }

  private void interceptBefore(TaskNode taskNode) {
    interceptors.forEach(interceptor -> interceptor.beforeExecute(taskNode));
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

    //1 更新节点状态
    nodeRecordRepository.updateNodeRecord(nodeRecord);

    //2 根据节点状态判断整个流水线状态
    NodeRecordDto record = nodeRecordRepository.getRecordById(nodeRecord.getRecordId());

    //3 根据historyId关联的任务来执行下一个任务
    PipelineTask pipelineTask = pipelineTaskMap.get(nodeRecord.getHistoryId());
    if (Objects.isNull(pipelineTask)) {
      log.info("not find Pipeline task historyId={}", nodeRecord.getHistoryId());
      return;
    }

    pipelineEndProcessor.statusChange(nodeRecord.getHistoryId(), record.getNodeId(),
        ProcessStatus.exchange(nodeRecord.getStatus()), pipelineTask.getLogId());

    //3.1 继续递归执行下一个任务
    runTaskNodeFromPipeline(pipelineTask);
  }

  @Override
  @Subscribe
  public void handle(InnerEvent event) {
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
}
