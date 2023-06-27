package com.zj.client.pipeline.executer;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.Subscribe;
import com.zj.client.entity.vo.NodeRecord;
import com.zj.client.notify.IResultEventNotify;
import com.zj.common.enums.NotifyType;
import com.zj.common.model.ResultEvent;
import com.zj.client.pipeline.executer.notify.IStatusNotifyListener;
import com.zj.client.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.monitor.trace.TidInterceptor;
import com.zj.common.utils.IpUtils;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@Slf4j
@Component
public class ExecuteProxy implements IStatusNotifyListener {

  @Autowired
  private NodeExecutor nodeExecutor;

  @Autowired
  private IResultEventNotify resultEventNotify;

  @Autowired
  @Qualifier("pipelineExecutorPool")
  private Executor executorService;

  /**
   * 流水线的执行应该是每个节点做为一个任务，这样就可以充分使用client的扩展性
   */
  public void runNode(TaskNode taskNode) {
    String traceId = MDC.get(TidInterceptor.MDC_TID_KEY);
    CompletableFuture.supplyAsync(() -> {
      if (Objects.isNull(taskNode)) {
        return null;
      }
      nodeExecutor.runNodeTask(taskNode.getHistoryId(), taskNode);
      return taskNode.getRecordId();
    }, executorService).whenComplete((node, e) -> {
      log.info("complete trigger action recordId = {} traceId={}", JSON.toJSONString(node), traceId);
    });
  }

  @Override
  @Subscribe
  public void statusChange(PipelineStatusEvent event) {
    log.info("receive event bus notify ={}", JSON.toJSONString(event));
    TaskNode taskNode = event.getTaskNode();
    //如果节点配置跳过，则修改状态为IGNORE
    ProcessStatus processStatus = event.getProcessStatus();
    if (processStatus.isFailStatus() && taskNode.getNodeConfig().isIgnoreError()) {
      processStatus = ProcessStatus.IGNORE_FAIL;
    }

    NodeRecord nodeRecord = new NodeRecord();
    nodeRecord.setHistoryId(taskNode.getHistoryId());
    String message = JSON.toJSONString(event.getErrorMsg());
    nodeRecord.setResult(message);

    ResultEvent resultEvent = new ResultEvent().executeId(taskNode.getRecordId())
        .notifyType(NotifyType.UPDATE_NODE_RECORD)
        .status(processStatus)
        .logId(taskNode.getLogId())
        .executeType(taskNode.getExecuteType())
        .params(nodeRecord)
        .clientIp(IpUtils.getLocalIP())
        .masterIP(taskNode.getMasterIp());
    resultEventNotify.notifyEvent(resultEvent);
  }
}
