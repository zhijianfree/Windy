package com.zj.master.dispatch.pipeline;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.master.dispatch.ClientProxy;
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
public class ExecuteProxy {

  @Autowired
  private ClientProxy clientProxy;

  @Autowired
  @Qualifier("pipelineExecutorPool")
  private ExecutorService executorService;
  @Autowired
  private INodeRecordRepository nodeRecordRepository;

  @Autowired
  private PipelineEndProcessor pipelineEndProcessor;

  private final Map<String, PipelineTask> pipelineTaskMap = new ConcurrentHashMap<>();

  public void runTask(PipelineTask pipelineTask) {
    CompletableFuture.supplyAsync(() -> {
      LinkedBlockingQueue<TaskNode> taskNodeQueue = pipelineTask.getTaskNodes();
      TaskNode taskNode = taskNodeQueue.poll();
      if (Objects.isNull(taskNode)) {
        return null;
      }

      clientProxy.sendPipelineNodeTask(taskNode);
      pipelineTaskMap.put(pipelineTask.getHistoryId(), pipelineTask);
      return taskNode;
    }, executorService).whenComplete((node, e) -> {
      String recordId = Optional.ofNullable(node).map(TaskNode::getRecordId).orElse("empty");
      log.info("complete trigger action recordId = {}", recordId);
    });
  }


  public void statusChange(String historyId, String recordId, Integer status, String message) {
    //todo 如果节点配置跳过，则修改状态为IGNORE
//    if (processStatus.isFailStatus() && taskNode.getNodeConfig().isIgnoreError()) {
//      processStatus = ProcessStatus.IGNORE_FAIL;
//    }

    //1 更新节点状态
    nodeRecordRepository.updateNodeRecordStatus(recordId, status, message);

    //根据节点状态判断整个流水线状态
    NodeRecord record = nodeRecordRepository.getRecordById(recordId);
    pipelineEndProcessor.statusChange(historyId, record.getNodeId(), ProcessStatus.exchange(status));

    PipelineTask pipelineTask = pipelineTaskMap.get(historyId);
    if (Objects.isNull(pipelineTask)) {
      log.info("can not find Pipeline task historyId={}", historyId);
      return;
    }

    //继续递归执行下一个任务
    runTask(pipelineTask);
  }
}
