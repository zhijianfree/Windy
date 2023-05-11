package com.zj.pipeline.executer;

import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import com.zj.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.pipeline.executer.notify.PipelineEventFactory;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.pipeline.executer.vo.TaskNode;
import com.zj.pipeline.executer.vo.TaskNodeRecord;
import com.zj.pipeline.service.NodeRecordService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  public static final String TRIGGER_TASK_ERROR = "trigger task error";
  private final List<INodeExecuteInterceptor> interceptors;
  private final Map<String, IRemoteInvoker> invokerMap;
  private final NodeRecordService nodeRecordService;
  private final UniqueIdService uniqueIdService;

  public NodeExecutor(List<INodeExecuteInterceptor> interceptors, List<IRemoteInvoker> invokers,
      NodeRecordService nodeRecordService, UniqueIdService uniqueIdService) {
    this.interceptors = interceptors;
    invokerMap = invokers.stream()
        .collect(Collectors.toMap(invoker -> invoker.type().name(), invoker -> invoker));
    this.nodeRecordService = nodeRecordService;
    this.uniqueIdService = uniqueIdService;
  }

  /**
   * 单个节点的执行逻辑主要涉及记录状态、调用第三方接口、查询任务状态（拦截器中实现）
   */
  public void runNodeTask(String historyId, TaskNode node) {
    if (CollectionUtils.isNotEmpty(interceptors)) {
      interceptors.forEach(interceptor -> interceptor.before(node));
    }

    log.info("start run task recordId={}", historyId);
    String recordId = uniqueIdService.getUniqueId();
    node.setRecordId(recordId);
    AtomicReference<ProcessStatus> statusAtomic = new AtomicReference<>(ProcessStatus.RUNNING);
    TaskNodeRecord taskNodeRecord = TaskNodeRecord.builder().historyId(historyId).recordId(recordId)
        .status(statusAtomic.get().getType()).nodeId(node.getNodeId()).build();
    nodeRecordService.saveTaskNodeRecord(taskNodeRecord);

    List<String> errorMsg = Collections.singletonList(TRIGGER_TASK_ERROR);
    try {
      IRemoteInvoker remoteInvoker = invokerMap.get(node.getExecuteType());
      if (Objects.isNull(remoteInvoker)) {
        throw new RuntimeException("can not find remote invoker");
      }

      boolean executeFlag = remoteInvoker.triggerRun(node.getRequestContext(), recordId);
      if (!executeFlag) {
        statusAtomic.set(ProcessStatus.FAIL);
        notifyNodeEvent(node, statusAtomic.get(), errorMsg);
      }
      log.info("task node run complete result={}", executeFlag);
    } catch (Exception e) {
      log.error("execute pipeline node error recordId={}", recordId, e);
      //如果请求失败则直接流水线终止
      statusAtomic.set(ProcessStatus.FAIL);
      errorMsg = getErrorMsg(e);
    }

    if (CollectionUtils.isNotEmpty(interceptors)) {
      interceptors.forEach(interceptor -> interceptor.after(node, statusAtomic.get()));
    }

    if (statusAtomic.get().isFailStatus()) {
      notifyNodeEvent(node, statusAtomic.get(), errorMsg);
    }
  }

  private List<String> getErrorMsg(Exception exception) {
    List<String> msg = new ArrayList<>();
    msg.add("trigger node task error: " + exception.toString());
    for (StackTraceElement element : exception.getStackTrace()) {
      msg.add(element.toString());
    }
    return msg;
  }


  private void notifyNodeEvent(TaskNode node, ProcessStatus status, List<String> errorMsg) {
    log.info("shutdown pipeline recordId={}", node.getRecordId());
    PipelineStatusEvent event = PipelineStatusEvent.builder().taskNode(node).processStatus(status)
        .errorMsg(errorMsg).build();
    PipelineEventFactory.sendNotifyEvent(event);
  }
}
