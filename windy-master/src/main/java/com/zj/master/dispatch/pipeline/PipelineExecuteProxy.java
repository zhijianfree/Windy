package com.zj.master.dispatch.pipeline;

import com.alibaba.fastjson.JSON;
import com.google.common.eventbus.Subscribe;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.IpUtils;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.master.dispatch.ClientProxy;
import com.zj.master.dispatch.listener.IInnerEventListener;
import com.zj.master.dispatch.listener.InnerEvent;
import com.zj.common.model.StopDispatch;
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

  private final Map<String, PipelineTask> pipelineTaskMap = new ConcurrentHashMap<>();

  public void runTask(PipelineTask pipelineTask) {
    log.info("start run task ={}", JSON.toJSONString(pipelineTask));
    CompletableFuture.supplyAsync(() -> {
      LinkedBlockingQueue<TaskNode> taskNodeQueue = pipelineTask.getTaskNodes();
      TaskNode taskNode = taskNodeQueue.poll();
      if (Objects.isNull(taskNode)) {
        return null;
      }

      taskNode.setDispatchType(DISPATCH_PIPELINE_TYPE);
      taskNode.setMasterIp(IpUtils.getLocalIP());
      clientProxy.sendDispatchTask(taskNode);

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


  public void statusChange(NodeRecordDto nodeRecord) {
    //todo 如果节点配置跳过，则修改状态为IGNORE
//    if (processStatus.isFailStatus() && taskNode.getNodeConfig().isIgnoreError()) {
//      processStatus = ProcessStatus.IGNORE_FAIL;
//    }

    //1 更新节点状态
    nodeRecordRepository.updateNodeRecord(nodeRecord);

    //2 根据节点状态判断整个流水线状态
    NodeRecord record = nodeRecordRepository.getRecordById(nodeRecord.getRecordId());
    pipelineEndProcessor.statusChange(nodeRecord.getHistoryId(), record.getNodeId(),
        ProcessStatus.exchange(nodeRecord.getStatus()));

    //3 根据historyId关联的任务来执行下一个任务
    PipelineTask pipelineTask = pipelineTaskMap.get(nodeRecord.getHistoryId());
    if (Objects.isNull(pipelineTask)) {
      log.info("not find Pipeline task historyId={}", nodeRecord.getHistoryId());
      return;
    }

    //3.1 继续递归执行下一个任务
    runTask(pipelineTask);
  }

  @Override
  @Subscribe
  public void handle(InnerEvent event) {
    if (!Objects.equals(event.getLogType().getType(), LogType.PIPELINE.getType())) {
      return;
    }

    PipelineTask pipelineTask = pipelineTaskMap.remove(event.getTargetId());
    if (Objects.isNull(pipelineTask)) {
      log.info("remove pipeline task but not find it ={}", event.getTargetId());
      return;
    }

    //只有流水线的执行才需要通知到client
    StopDispatch stopDispatch = new StopDispatch();
    stopDispatch.setLogType(event.getLogType());
    stopDispatch.setTargetId(event.getTargetId());
    clientProxy.stopDispatchTask(stopDispatch);
    log.info("stop pipeline task pipelineId={} historyId={}", pipelineTask.getPipelineId(),
        pipelineTask.getHistoryId());

    pipelineHistoryRepository.updateStatus(event.getTargetId(), ProcessStatus.STOP);
  }
}
