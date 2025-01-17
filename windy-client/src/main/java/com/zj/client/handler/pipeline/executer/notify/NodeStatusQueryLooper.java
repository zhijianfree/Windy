package com.zj.client.handler.pipeline.executer.notify;

import com.alibaba.fastjson.JSON;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.entity.bo.DelayQuery;
import com.zj.common.entity.feature.CompareDefine;
import com.zj.client.handler.feature.executor.compare.CompareOperator;
import com.zj.common.entity.feature.CompareResult;
import com.zj.client.handler.feature.executor.compare.operator.CompareFactory;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.CompareInfo;
import com.zj.client.handler.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ProcessStatus;
import com.zj.plugin.loader.ExecuteStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/3/30
 */
@Slf4j
@Component
public class NodeStatusQueryLooper implements Runnable {

  public static final String DESCRIPTION_FORMAT = "比较响应字段:【%s】- 描述信息:%s";
  public static final String EXPECT_VALUE_FORMAT = "期待值:【%s】";
  public static final String RESULT_VALUE_FORMAT = "返回值:【%s】";
  public static final String OPERATOR_FORMAT = "操作符:【%s】";
  public static final String QUERY_ERROR_TIPS = "loop query status error";
  public static final int QUERY_INTERVAL_SECONDS = 5;
  private final Map<String, INodeTrigger> remoteInvokerMap;
  private final Map<String, Long> stopPipelineHistoryMap = new ConcurrentHashMap<>();
  private final DelayQueue<DelayQuery> delayQueue = new DelayQueue<>();
  private final Executor executorService;
  private final CompareFactory compareFactory;
  private final GlobalEnvConfig globalEnvConfig;


  public NodeStatusQueryLooper(List<INodeTrigger> remoteInvokers,
      @Qualifier("loopQueryPool") Executor executorService, CompareFactory compareFactory,
      GlobalEnvConfig globalEnvConfig) {
    remoteInvokerMap = remoteInvokers.stream()
        .collect(Collectors.toMap(invoker -> invoker.type().name(), invoker -> invoker));
    this.executorService = executorService;
    this.compareFactory = compareFactory;
    this.globalEnvConfig = globalEnvConfig;
    new Thread(this, "Loop-Query-Thread").start();
  }

  public void addQueryTask(TaskNode node) {
    delayQueue.offer(new DelayQuery(node, QUERY_INTERVAL_SECONDS));
  }

  public Integer getWaitQuerySize() {
    return delayQueue.size();
  }

  private void runNode(TaskNode node) {
    executorService.execute(() -> {
      try {
        INodeTrigger remoteInvoker = remoteInvokerMap.get(node.getExecuteType());
        QueryResponseModel queryResponse = remoteInvoker.queryStatus(node.getRefreshContext(), node);
        log.info("get query status result={}", JSON.toJSONString(queryResponse));
        if (Objects.isNull(queryResponse)) {
          handleDefaultError(node);
          return;
        }

        if (stopPipelineHistoryMap.containsKey(node.getHistoryId())) {
          log.info("record result ignore , because pipeline is stop historyId={} recordId={}",
              node.getHistoryId(), node.getRecordId());
          stopPipelineHistoryMap.remove(node.getHistoryId());
          return;
        }

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
      } catch (Exception e) {
        log.info(QUERY_ERROR_TIPS, e);
        handleDefaultError(node);
      }
    });
  }

  private void handleDefaultError(TaskNode node) {
    QueryResponseModel queryResponse = new QueryResponseModel();
    queryResponse.setStatus(ProcessStatus.FAIL.getType());
    queryResponse.setData(new Object());
    queryResponse.setMessage(Collections.singletonList(QUERY_ERROR_TIPS));
    handleRecordFinalStatus(node, queryResponse);
  }

  private void handleRecordFinalStatus(TaskNode node, QueryResponseModel response) {
    compareResultWithExpect(node, response);
    notifyStatus(node, response);
  }

  /**
   * 将查询的成功的结果与配置的期望值比较
   */
  private void compareResultWithExpect(TaskNode node, QueryResponseModel responseModel) {
    //如果没有断言比较直接退出
    List<CompareInfo> compareConfigs = node.getRefreshContext().getCompareConfig();
    if (CollectionUtils.isEmpty(compareConfigs) || Objects.isNull(responseModel.getData())) {
      log.info("compare info is empty or response data is null");
      return;
    }

    Map<String, Object> map = JSON.parseObject(JSON.toJSONString(responseModel.getData()));
    boolean anyMatch = compareConfigs.stream().anyMatch(compareInfo -> {
      CompareResult compareResult = handleCompare(map, compareInfo);
      if (!compareResult.isCompareSuccess()) {
        responseModel.setStatus(ProcessStatus.FAIL.getType());
        List<String> tips = exchangeTips(map, compareInfo);
        responseModel.getMessage().addAll(tips);
      }
      return !compareResult.isCompareSuccess();
    });

    //如果比较值都成功，那么就可以判断当前任务状态为成功
    if (!anyMatch) {
      responseModel.setStatus(ProcessStatus.SUCCESS.getType());
    }
    log.info("feature result compare with expect = {}", !anyMatch);
  }

  private CompareResult handleCompare(Map<String, Object> response, CompareInfo compareInfo) {
    CompareOperator compareOperator = compareFactory.getOperator(compareInfo.getOperator());
    CompareDefine compareDefine = new CompareDefine();
    compareDefine.setResponseValue(response.get(compareInfo.getCompareKey()));
    compareDefine.setExpectValue(compareInfo.getValue());
    return compareOperator.compare(compareDefine);
  }

  private List<String> exchangeTips(Map<String, Object> response, CompareInfo compareInfo) {
    if (MapUtils.isEmpty(response)) {
      return Collections.emptyList();
    }
    String desc = String.format(DESCRIPTION_FORMAT, compareInfo.getCompareKey(),
        compareInfo.getDescription());
    String expectDesc = String.format(EXPECT_VALUE_FORMAT, compareInfo.getValue());
    String operatorDesc = String.format(OPERATOR_FORMAT, compareInfo.getOperator());
    String resultDesc = String.format(RESULT_VALUE_FORMAT,
        response.get(compareInfo.getCompareKey()));
    return Arrays.asList(desc, resultDesc, operatorDesc, expectDesc);
  }

  private static void notifyStatus(TaskNode node, QueryResponseModel responseModel) {
    PipelineStatusEvent statusEvent = PipelineStatusEvent.builder()
        .taskNode(node)
        .processStatus(ProcessStatus.exchange(responseModel.getStatus()))
        .errorMsg(responseModel.getMessage())
        .context(responseModel.getContext())
        .build();
    PipelineEventFactory.sendNotifyEvent(statusEvent);
  }

  private void cycleRunTask(TaskNode node) {
    if (checkRunTimeout(node)) {
      log.info("node record run timeout recordId={}", node.getRecordId());
      QueryResponseModel queryResponseModel = new QueryResponseModel();
      queryResponseModel.setStatus(ProcessStatus.TIMEOUT.getType());
      queryResponseModel.addMessage(ProcessStatus.TIMEOUT.getDesc());
      handleRecordFinalStatus(node, queryResponseModel);
      putAndCheckRecord(node.getRecordId());
      return;
    }
    addQueryTask(node);
  }

  /**
   * 任务执行超过最大超时时间退出循环查询
   */
  private boolean checkRunTimeout(TaskNode taskNode) {
    long dateNow = System.currentTimeMillis();
    long mills = dateNow - taskNode.getExecuteTime();
    Long timeout = Optional.ofNullable(taskNode.getExpireTime()).orElseGet(globalEnvConfig::getLoopQueryTimeout) ;
    return mills >= timeout;
  }

  public void run() {
    while (true) {
      DelayQuery delayQuery = null;
      try {
        delayQuery = delayQueue.poll(100, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        log.info("get task from queue error", e);
      }

      if (Objects.nonNull(delayQuery)) {
        runNode(delayQuery.getTaskNode());
      }
    }
  }

  public void stopPipeline(String historyId) {
    putAndCheckRecord(historyId);
    delayQueue.removeIf(delayQuery -> Objects.equals(historyId, delayQuery.getTaskNode().getHistoryId()));
    log.info("add stop query historyId={}", historyId);
  }

  private void putAndCheckRecord(String historyId) {
    Long dateNow = System.currentTimeMillis();
    stopPipelineHistoryMap.put(historyId, dateNow);
  }
}
