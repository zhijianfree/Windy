package com.zj.pipeline.executer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.eventbus.Subscribe;
import com.zj.pipeline.entity.po.NodeRecord;
import com.zj.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.pipeline.executer.enums.ProcessStatus;
import com.zj.pipeline.executer.notify.PipelineEventFactory;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.pipeline.executer.vo.TaskNode;
import com.zj.pipeline.service.PipelineNodeRecordService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/3/30
 */
@Slf4j
@Component
public class NodeStatusQueryLooper implements IStatusNotifyListener, Runnable {

  public static final int MAX_REMOVE_TIME = 2 * 60 * 60 * 1000;
  public static final int CORE_POOL_SIZE = 20;
  public static final int MAXIMUM_POOL_SIZE = 60;
  public static final int KEEP_ALIVE_TIME = 10;
  public static final int POOL_QUEUE_CAPACITY = 1000;

  private final Map<String, Long> ignoreMap = new ConcurrentHashMap<>();

  private LinkedBlockingQueue<TaskNode> queue = new LinkedBlockingQueue<TaskNode>();
  private ExecutorService executorService = new ThreadPoolExecutor(CORE_POOL_SIZE,
      MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MINUTES,
      new LinkedBlockingQueue<>(POOL_QUEUE_CAPACITY), new CallerRunsPolicy());

  private final PipelineNodeRecordService nodeRecordService;
  private final IRemoteInvoker remoteInvoker;

  public NodeStatusQueryLooper(PipelineNodeRecordService nodeRecordService,
      IRemoteInvoker remoteInvoker) {
    this.nodeRecordService = nodeRecordService;
    this.remoteInvoker = remoteInvoker;
    new Thread(this).start();
  }

  public void addQuestTask(TaskNode node) {
    queue.add(node);
  }

  private void runNode(TaskNode node) {
    executorService.execute(() -> {
      String result = remoteInvoker.queryStatus(node.getRefreshContext(), node.getRecordId());
      log.info("get query status result={}", result);
      JSONObject jsonObject = JSON.parseObject(result);
      JSONObject data = jsonObject.getJSONObject("data");
      Integer status = data.getInteger("status");
      if (Objects.isNull(status)) {
        log.info("get task record status is empty. recordId={}", node.getRecordId());
        cycleRunTask(node);
        return;
      }
      if (!Objects.equals(ProcessStatus.RUNNING.getType(), status)) {
        nodeRecordService.updateTaskNodeStatus(node.getRecordId(), status);

        PipelineStatusEvent statusEvent = PipelineStatusEvent.builder().recordId(node.getRecordId())
            .nodeId(node.getNodeId()).processStatus(ProcessStatus.exchange(status)).build();
        PipelineEventFactory.sendNotifyEvent(statusEvent);
        return;
      }
      cycleRunTask(node);
    });
  }

  private void cycleRunTask(TaskNode node) {
    if (ignoreMap.containsKey(node.getNodeId())) {
      log.info("find ignore query task recordId={}", node.getRecordId());
      return;
    }

    Long dateNow = System.currentTimeMillis();
    long mills = dateNow - node.getExecuteTime();
    if (mills < MAX_REMOVE_TIME) {
      queue.add(node);
    }
  }

  public void run() {
    while (true) {
      TaskNode taskNode = null;
      try {
        taskNode = queue.poll(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        log.info("get task from queue error", e);
      }

      if (Objects.nonNull(taskNode)) {
        runNode(taskNode);
      }
    }
  }

  @Subscribe
  public void statusChange(PipelineStatusEvent event) {
    log.info("receive pipeline notify={}", JSON.toJSONString(event));
    if (!event.getProcessStatus().isFailStatus()) {
      return;
    }

    NodeRecord nodeRecord = nodeRecordService.getNodeRecord(event.getRecordId());
    List<NodeRecord> recordList = nodeRecordService.list(Wrappers.lambdaQuery(NodeRecord.class)
        .eq(NodeRecord::getHistoryId, nodeRecord.getHistoryId()));
    List<String> recordNodeIds = recordList.stream().map(NodeRecord::getRecordId)
        .collect(Collectors.toList());
    queue.removeIf(node -> recordNodeIds.contains(node.getRecordId()));

    Long dateNow = System.currentTimeMillis();
    recordNodeIds.forEach(nodeId -> {
      ignoreMap.put(nodeId, dateNow);
    });

    ignoreMap.entrySet().removeIf(entity -> {
      long mills = entity.getValue() - dateNow;
      return mills > NodeStatusQueryLooper.MAX_REMOVE_TIME;
    });
  }
}
