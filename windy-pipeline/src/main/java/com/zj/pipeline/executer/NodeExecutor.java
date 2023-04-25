package com.zj.pipeline.executer;

import com.alibaba.fastjson.JSON;
import com.zj.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.pipeline.executer.enums.ProcessStatus;
import com.zj.pipeline.executer.notify.PipelineEventFactory;
import com.zj.pipeline.executer.vo.NodeConfig;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.pipeline.executer.vo.TaskNode;
import com.zj.pipeline.executer.vo.TaskNodeRecord;
import com.zj.pipeline.service.PipelineNodeRecordService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/3/30
 */
@Slf4j
@Component
public class NodeExecutor {

  private final List<INodeExecuteInterceptor> interceptors;
  private final Map<String, IRemoteInvoker> invokerMap;
  private final PipelineNodeRecordService pipelineNodeRecordService;

  public NodeExecutor(List<INodeExecuteInterceptor> interceptors, List<IRemoteInvoker> invokers,
      PipelineNodeRecordService pipelineNodeRecordService) {
    this.interceptors = interceptors;
    invokerMap = invokers.stream()
        .collect(Collectors.toMap(IRemoteInvoker::type, invoker -> invoker));
    this.pipelineNodeRecordService = pipelineNodeRecordService;
  }

  /**
   * 单个节点的执行逻辑主要涉及记录状态、调用第三方接口、查询任务状态（拦截器中实现）
   */
  public void runNodeTask(String historyId, TaskNode node) {
    if (CollectionUtils.isNotEmpty(interceptors)) {
      interceptors.forEach(interceptor -> interceptor.before(node));
    }

    log.info("start run task recordId={}", historyId);
    String recordId = UUID.randomUUID().toString();
    node.setRecordId(recordId);
    AtomicReference<ProcessStatus> statusAtomic = new AtomicReference<>(ProcessStatus.RUNNING);
    TaskNodeRecord taskNodeRecord = TaskNodeRecord.builder().historyId(historyId).recordId(recordId)
        .status(statusAtomic.get().getType()).nodeId(node.getNodeId()).build();
    pipelineNodeRecordService.saveTaskNodeRecord(taskNodeRecord);

    try {
      IRemoteInvoker remoteInvoker = invokerMap.get(node.getExecuteType());
      if (Objects.isNull(remoteInvoker)) {
        throw new RuntimeException("can not find remote invoker");
      }

      boolean executeFlag = remoteInvoker.triggerRun(node.getRequestContext(), recordId);
      if (!executeFlag) {
        statusAtomic.set(ProcessStatus.FAIL);
        notifyNodeEvent(recordId, node, taskNodeRecord);
      }
      log.info("task node run complete result={}", executeFlag);
    } catch (Exception e) {
      log.error("execute pipeline node error recordId={}", recordId, e);
      //如果请求失败则直接流水线终止
      statusAtomic.set(ProcessStatus.FAIL);
      String errorMsg = getErrorMsg(e);
      taskNodeRecord.setResult(errorMsg);
    }

    //保存node节点执行开始
    taskNodeRecord.setStatus(statusAtomic.get().getType());
    pipelineNodeRecordService.updateNodeRecord(taskNodeRecord);

    if (CollectionUtils.isNotEmpty(interceptors)) {
      interceptors.forEach(interceptor -> interceptor.after(node, statusAtomic.get()));
    }

    if (statusAtomic.get().isFailStatus()) {
      notifyNodeEvent(recordId, node, taskNodeRecord);
    }
  }

  private String getErrorMsg(Exception exception) {
    List<String> msg = new ArrayList<>();
    msg.add("trigger node task error: " + exception.toString());
    for (StackTraceElement element : exception.getStackTrace()) {
      msg.add(element.toString());
    }
    return JSON.toJSONString(msg);
  }


  private void notifyNodeEvent(String recodeId, TaskNode node, TaskNodeRecord taskNodeRecord) {
    NodeConfig nodeConfig = node.getNodeConfig();
    if (nodeConfig.isIgnoreError()) {
      log.info("pipeline ignore error recordId={}", recodeId);
      taskNodeRecord.setStatus(ProcessStatus.IGNORE_FAIL.getType());
      pipelineNodeRecordService.updateNodeRecord(taskNodeRecord);
      return;
    }

    log.info("shutdown pipeline recordId={}", recodeId);
    PipelineStatusEvent event = PipelineStatusEvent.builder().historyId(node.getHistoryId())
        .recordId(recodeId).nodeId(node.getNodeId()).processStatus(ProcessStatus.FAIL).build();
    PipelineEventFactory.sendNotifyEvent(event);
  }
}
