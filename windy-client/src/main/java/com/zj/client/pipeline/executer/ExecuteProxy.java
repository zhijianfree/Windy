package com.zj.client.pipeline.executer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.eventbus.Subscribe;
import com.zj.client.notify.IResultEventNotify;
import com.zj.client.notify.NotifyType;
import com.zj.client.pipeline.executer.notify.IStatusNotifyListener;
import com.zj.client.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ProcessStatus;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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
public class ExecuteProxy implements IStatusNotifyListener {

  @Autowired
  private NodeExecutor nodeExecutor;

  @Autowired
  private IResultEventNotify resultEventNotify;

  @Autowired
  @Qualifier("pipelineExecutorPool")
  private ExecutorService executorService;

  /**
   * 流水线的执行应该是每个节点做为一个任务，这样就可以充分使用client的扩展性
   */
  public void runNode(TaskNode taskNode) {
    CompletableFuture.supplyAsync(() -> {
      if (Objects.isNull(taskNode)) {
        return null;
      }
      nodeExecutor.runNodeTask(taskNode.getHistoryId(), taskNode);
      return taskNode.getRecordId();
    }, executorService).whenComplete((node, e) -> {
      log.info("complete trigger action recordId = {}", JSON.toJSONString(node));
    });
  }

  @Override
  @Subscribe
  public void statusChange(PipelineStatusEvent event) {
    TaskNode taskNode = event.getTaskNode();
    //如果节点配置跳过，则修改状态为IGNORE
    ProcessStatus processStatus = event.getProcessStatus();
    if (processStatus.isFailStatus() && taskNode.getNodeConfig().isIgnoreError()) {
      processStatus = ProcessStatus.IGNORE_FAIL;
    }

    String message = JSON.toJSONString(event.getErrorMsg());
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("message", message);
    resultEventNotify
        .notify(taskNode.getRecordId(), NotifyType.UPDATE_NODE_RECORD, processStatus, jsonObject);
  }
}
