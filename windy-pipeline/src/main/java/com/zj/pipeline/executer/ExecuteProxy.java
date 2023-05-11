package com.zj.pipeline.executer;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.Subscribe;
import com.zj.common.enums.ProcessStatus;
import com.zj.pipeline.entity.vo.PipelineTask;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.pipeline.executer.vo.TaskNode;
import com.zj.pipeline.service.NodeRecordService;
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
public class ExecuteProxy implements IStatusNotifyListener{

  @Autowired
  private NodeExecutor nodeExecutor;

  @Autowired
  @Qualifier("pipelineExecutorPool")
  private ExecutorService executorService;
  @Autowired
  private NodeRecordService nodeRecordService;

  private final Map<String, PipelineTask> pipelineTaskMap = new ConcurrentHashMap<>();

  public void execute(PipelineTask pipelineTask) {
    runNode(pipelineTask);
  }

  private void runNode(PipelineTask pipelineTask) {
    CompletableFuture.supplyAsync(() -> {
      LinkedBlockingQueue<TaskNode> taskNodeQueue = pipelineTask.getTaskNodes();
      TaskNode taskNode = taskNodeQueue.poll();
      if (Objects.isNull(taskNode)) {
        return null;
      }
      nodeExecutor.runNodeTask(pipelineTask.getHistoryId(), taskNode);
      pipelineTaskMap.put(pipelineTask.getHistoryId(), pipelineTask);
      return taskNode;
    }, executorService).whenComplete((node, e) -> {
      String recordId = Optional.ofNullable(node).map(TaskNode::getRecordId).orElse("empty");
      log.info("complete trigger action recordId = {}", recordId);
    });
  }

  @Override
  @Subscribe
  public void statusChange(PipelineStatusEvent event) {
    updateNodeStatus(event);

    TaskNode taskNode = event.getTaskNode();
    PipelineTask pipelineTask = pipelineTaskMap.get(taskNode.getHistoryId());
    if (Objects.isNull(pipelineTask)) {
      log.info("can not find Pipeline task historyId={}", taskNode.getHistoryId());
      return;
    }
    runNode(pipelineTask);
  }

  private void updateNodeStatus(PipelineStatusEvent event) {
    TaskNode taskNode = event.getTaskNode();
    //如果节点配置跳过，则修改状态为IGNORE
    ProcessStatus processStatus = event.getProcessStatus();
    if (processStatus.isFailStatus() && taskNode.getNodeConfig().isIgnoreError()) {
      processStatus = ProcessStatus.IGNORE_FAIL;
    }

    String message = JSON.toJSONString(event.getErrorMsg());
    nodeRecordService.updateNodeRecordStatus(taskNode.getRecordId(), processStatus.getType(), message);
  }
}
