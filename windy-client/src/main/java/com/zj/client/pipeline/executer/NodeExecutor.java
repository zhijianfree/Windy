package com.zj.client.pipeline.executer;

import com.alibaba.fastjson.JSONObject;
import com.zj.client.notify.IResultEventNotify;
import com.zj.common.enums.NotifyType;
import com.zj.common.model.ResultEvent;
import com.zj.client.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.client.pipeline.executer.intercept.INodeExecuteInterceptor;
import com.zj.client.pipeline.executer.notify.PipelineEventFactory;
import com.zj.client.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.client.pipeline.executer.vo.RequestContext;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.client.pipeline.executer.vo.TaskNodeRecord;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class NodeExecutor {

  public static final String TRIGGER_TASK_ERROR = "trigger task error";
  private final List<INodeExecuteInterceptor> interceptors;
  private final Map<String, IRemoteInvoker> invokerMap;
  private final UniqueIdService uniqueIdService;
  private final IResultEventNotify resultEventNotify;

  public NodeExecutor(List<INodeExecuteInterceptor> interceptors, List<IRemoteInvoker> invokers,
      UniqueIdService uniqueIdService, IResultEventNotify resultEventNotify) {
    this.interceptors = interceptors;
    invokerMap = invokers.stream()
        .collect(Collectors.toMap(invoker -> invoker.type().name(), invoker -> invoker));
    this.uniqueIdService = uniqueIdService;
    this.resultEventNotify = resultEventNotify;
  }

  /**
   * 单个节点的执行逻辑主要涉及记录状态、调用第三方接口、查询任务状态（拦截器中实现）
   */
  public void runNodeTask(String historyId, TaskNode node) {
    log.info("start run task recordId={}", historyId);
    interceptors.forEach(interceptor -> interceptor.before(node));

    String recordId = uniqueIdService.getUniqueId();
    node.setRecordId(recordId);
    AtomicReference<ProcessStatus> statusAtomic = new AtomicReference<>(ProcessStatus.RUNNING);
    saveRecord(historyId, node, recordId, statusAtomic);
    List<String> errorMsg = Collections.singletonList(TRIGGER_TASK_ERROR);
    try {
      IRemoteInvoker remoteInvoker = invokerMap.get(node.getExecuteType());
      if (Objects.isNull(remoteInvoker)) {
        throw new RuntimeException("can not find remote invoker");
      }
      JSONObject context = node.getRequestContext();
      RequestContext requestContext = new RequestContext(context);
      boolean executeFlag = remoteInvoker.triggerRun(requestContext, recordId);
      if (!executeFlag) {
        statusAtomic.set(ProcessStatus.FAIL);
        notifyNodeEvent(node, statusAtomic.get(), errorMsg);
        return;
      }
      log.info("task node run complete result={}", executeFlag);
    } catch (Exception e) {
      log.error("execute pipeline node error recordId={}", recordId, e);
      //如果请求失败则直接流水线终止
      statusAtomic.set(ProcessStatus.FAIL);
      errorMsg = getErrorMsg(e);
    }

    interceptors.forEach(interceptor -> interceptor.after(node, statusAtomic.get()));
    notifyNodeEvent(node, statusAtomic.get(), errorMsg);
  }

  private void saveRecord(String historyId, TaskNode node, String recordId,
      AtomicReference<ProcessStatus> statusAtomic) {
    long currentTimeMillis = System.currentTimeMillis();
    TaskNodeRecord taskNodeRecord = TaskNodeRecord.builder().historyId(historyId).recordId(recordId)
        .status(statusAtomic.get().getType()).nodeId(node.getNodeId()).createTime(currentTimeMillis)
        .updateTime(currentTimeMillis).build();

    ResultEvent resultEvent = new ResultEvent().executeId(recordId)
        .notifyType(NotifyType.CREATE_NODE_RECORD)
        .status(ProcessStatus.RUNNING)
        .params(taskNodeRecord)
        .masterIP(node.getMasterIp())
        .logId(node.getLogId());
    resultEventNotify.notifyEvent(resultEvent);
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
    if (!status.isFailStatus()) {
      return;
    }
    PipelineStatusEvent event = PipelineStatusEvent.builder().taskNode(node).processStatus(status)
        .errorMsg(errorMsg).build();
    PipelineEventFactory.sendNotifyEvent(event);
  }
}
