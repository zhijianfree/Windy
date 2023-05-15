package com.zj.pipeline.executer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.eventbus.Subscribe;
import com.zj.domain.entity.po.pipeline.NodeRecord;
import com.zj.domain.entity.dto.pipeline.CompareResult;
import com.zj.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.common.enums.ProcessStatus;
import com.zj.pipeline.executer.notify.PipelineEventFactory;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.pipeline.executer.vo.QueryResponseModel;
import com.zj.pipeline.executer.vo.TaskNode;
import com.zj.pipeline.service.NodeRecordService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class NodeStatusQueryLooper implements IStatusNotifyListener, Runnable {

  public static final int MAX_REMOVE_TIME = 2 * 60 * 60 * 1000;
  public static final String DESCRIPTION_FORMAT = "比较响应字段:【%s】- 描述信息:%s";
  public static final String EXPECT_VALUE_FORMAT = "期待值:【%s】";
  public static final String RESULT_VALUE_FORMAT = "返回值:【%s】";
  public static final String OPERATOR_FORMAT = "操作符:【%s】";

  private final Map<String, Long> timeoutRecordMap = new ConcurrentHashMap<>();
  private final Map<String, IRemoteInvoker> remoteInvokerMap;

  private CopyOnWriteArrayList<String> stopRecords = new CopyOnWriteArrayList<>();
  private final LinkedBlockingQueue<TaskNode> queue = new LinkedBlockingQueue<TaskNode>();
  @Autowired
  @Qualifier("queryLooperExecutorPool")
  private ExecutorService executorService;

  private final NodeRecordService nodeRecordService;


  public NodeStatusQueryLooper(NodeRecordService nodeRecordService,
      List<IRemoteInvoker> remoteInvokers) {
    this.nodeRecordService = nodeRecordService;
    remoteInvokerMap = remoteInvokers.stream()
        .collect(Collectors.toMap(invoker -> invoker.type().name(), invoker -> invoker));

    new Thread(this).start();
  }

  public void addQuestTask(TaskNode node) {
    queue.add(node);
  }

  private void runNode(TaskNode node) {
    executorService.execute(() -> {
      IRemoteInvoker remoteInvoker = remoteInvokerMap.get(node.getExecuteType());
      String result = remoteInvoker.queryStatus(node.getRefreshContext(), node.getRecordId());
      log.info("get query status result={}", result);

      if (stopRecords.contains(node.getRecordId())) {
        log.info("record result ignore , because pipeline is stop recordId={}", node.getRecordId());
        stopRecords.remove(node.getRecordId());
        return;
      }

      QueryResponseModel queryResponse = JSON.parseObject(result, QueryResponseModel.class);
      if (Objects.isNull(queryResponse.getStatus())) {
        log.info("get task record status is empty. recordId={}", node.getRecordId());
        cycleRunTask(node);
        return;
      }

      if (!Objects.equals(ProcessStatus.RUNNING.getType(), queryResponse.getStatus())) {
        handleRecordFinalStatus(node, queryResponse);
        return;
      }
      cycleRunTask(node);
    });
  }

  private void handleRecordFinalStatus(TaskNode node, QueryResponseModel response) {
    compareResultWithExpect(node, response);
    notifyStatus(node, response);
  }

  /**
   * 将查询的成功的结果与配置的期望值比较
   * */
  private void compareResultWithExpect(TaskNode node, QueryResponseModel responseModel) {
    if (!Objects.equals(responseModel.getStatus(), ProcessStatus.SUCCESS.getType())){
      return;
    }

    JSONObject jsonObject = responseModel.getData();
    List<CompareResult> compareConfigs = node.getRefreshContext().getCompareConfig();
    for (CompareResult compareResult : compareConfigs) {
      boolean isMatch = CompareUtils.isMatch(compareResult.getOperator(),
          jsonObject.get(compareResult.getCompareKey()), compareResult.getValue());
      if (!isMatch) {
        responseModel.setStatus(ProcessStatus.FAIL.getType());
        responseModel.setMessage(exchangeTips(jsonObject, compareResult));
        return;
      }
    }
  }

  private List<String> exchangeTips(JSONObject jsonObject, CompareResult compareResult) {
    String desc = String.format(DESCRIPTION_FORMAT, compareResult.getCompareKey(),
        compareResult.getDescription());
    String expectDesc = String.format(EXPECT_VALUE_FORMAT, compareResult.getValue());
    String operatorDesc = String.format(OPERATOR_FORMAT, compareResult.getOperator());
    String resultDesc = String.format(RESULT_VALUE_FORMAT,
        jsonObject.get(compareResult.getCompareKey()));
    return Arrays.asList(desc, resultDesc, operatorDesc, expectDesc);
  }

  private static void notifyStatus(TaskNode node, QueryResponseModel responseModel) {
    PipelineStatusEvent statusEvent = PipelineStatusEvent.builder().taskNode(node)
        .processStatus(ProcessStatus.exchange(responseModel.getStatus()))
        .errorMsg(responseModel.getMessage()).build();
    PipelineEventFactory.sendNotifyEvent(statusEvent);
  }

  private void cycleRunTask(TaskNode node) {
    if (timeoutRecordMap.containsKey(node.getNodeId())) {
      log.info("find ignore query task recordId={}", node.getRecordId());
      return;
    }

    Long dateNow = System.currentTimeMillis();
    long mills = dateNow - node.getExecuteTime();
    if (mills > MAX_REMOVE_TIME) {
      log.info("node record run timeout recordId={}", node.getRecordId());

      QueryResponseModel queryResponseModel = new QueryResponseModel();
      queryResponseModel.setStatus(ProcessStatus.TIMEOUT.getType());
      queryResponseModel.setMessage(Collections.singletonList(ProcessStatus.TIMEOUT.getDesc()));
      handleRecordFinalStatus(node, queryResponseModel);
      return;
    }

    queue.add(node);
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

    String historyId = event.getTaskNode().getHistoryId();
    if (Objects.equals(event.getProcessStatus().getType(), ProcessStatus.STOP.getType())) {
      List<NodeRecord> recordList = nodeRecordService.list(Wrappers.lambdaQuery(NodeRecord.class)
          .eq(NodeRecord::getHistoryId, historyId));
      List<String> recordIds = recordList.stream().map(NodeRecord::getRecordId)
          .collect(Collectors.toList());
      queue.removeIf(taskNode -> recordIds.contains(taskNode.getRecordId()));
      stopRecords.addAll(recordIds);
      nodeRecordService.batchUpdateStatus(recordIds, ProcessStatus.STOP);
      return;
    }

    List<NodeRecord> recordList = nodeRecordService.list(Wrappers.lambdaQuery(NodeRecord.class)
        .eq(NodeRecord::getHistoryId, historyId));
    List<String> recordNodeIds = recordList.stream().map(NodeRecord::getRecordId)
        .collect(Collectors.toList());
    queue.removeIf(node -> recordNodeIds.contains(node.getRecordId()));

    Long dateNow = System.currentTimeMillis();
    recordNodeIds.forEach(nodeId -> {
      timeoutRecordMap.put(nodeId, dateNow);
    });

    timeoutRecordMap.entrySet().removeIf(entity -> {
      long mills = entity.getValue() - dateNow;
      return mills > NodeStatusQueryLooper.MAX_REMOVE_TIME;
    });
  }
}
