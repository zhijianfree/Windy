package com.zj.master.dispatch.pipeline;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.Subscribe;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.model.StopDispatch;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.master.dispatch.ClientProxy;
import com.zj.master.dispatch.listener.IInnerEventListener;
import com.zj.master.dispatch.listener.InnerEvent;
import com.zj.master.entity.vo.TaskNode;
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
public class PipelineExecuteProxy implements IInnerEventListener {

  public static final String TASK_DONE_TIPS = "no task need run";
  public static final String DISPATCH_PIPELINE_TYPE = "PIPELINE";
  public static final String DISPATCH_TASK_STOP_URL = "http://%s/v1/devops/dispatch/stop";
  @Autowired
  private ClientProxy clientProxy;

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

  private final Map<String, PipelineTask> pipelineTaskMap = new ConcurrentHashMap<>();

  public void runTask(PipelineTask pipelineTask) {
    log.info("start run task ={}", JSON.toJSONString(pipelineTask));
    CompletableFuture.supplyAsync(() -> {
      TaskNode taskNode = pollAndCheckTask(pipelineTask);
      if (Objects.isNull(taskNode)) {
        return null;
      }

      String logId = pipelineTask.getLogId();
      taskNode.setLogId(logId);
      taskNode.setDispatchType(DISPATCH_PIPELINE_TYPE);
      taskNode.setMasterIp(IpUtils.getLocalIP());
      boolean dispatchResult = clientProxy.sendDispatchTask(taskNode);
      if (!dispatchResult){
        log.info("dispatch pipeline task to client fail ");
        pipelineHistoryRepository.updateStatus(taskNode.getHistoryId(), ProcessStatus.FAIL);
        dispatchLogRepository.updateLogStatus(logId, ProcessStatus.FAIL.getType());
        return null;
      }

      pipelineTaskMap.put(pipelineTask.getHistoryId(), pipelineTask);
      return taskNode;
    }, executorService).whenComplete((node, e) -> {
      String recordId = Optional.ofNullable(node).map(TaskNode::getRecordId).orElse(TASK_DONE_TIPS);
      log.info("complete trigger action recordId = {}", recordId);
    }).exceptionally((e) -> {
      log.error("handle task error", e);
      return null;
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
    runTask(pipelineTask);
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
    clientProxy.stopDispatchTask(stopDispatch);
    log.info("stop pipeline task historyId={}", historyId);
  }
}
