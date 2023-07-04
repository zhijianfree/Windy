package com.zj.client.handler.pipeline.executer.notify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.handler.feature.executor.compare.CompareDefine;
import com.zj.client.handler.feature.executor.compare.CompareOperator;
import com.zj.client.handler.feature.executor.compare.CompareResult;
import com.zj.client.handler.feature.executor.compare.operator.CompareFactory;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.CompareInfo;
import com.zj.client.handler.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.common.enums.ProcessStatus;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

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
  private final Map<String, INodeTrigger> remoteInvokerMap;
  private final Map<String, Long> stopPipelineHistoryMap = new ConcurrentHashMap<>();
  private final LinkedBlockingQueue<TaskNode> queue = new LinkedBlockingQueue<TaskNode>();
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

    new Thread(this).start();
  }

  public void addQueryTask(TaskNode node) {
    queue.add(node);
  }

  public Integer getWaitQuerySize() {
    return queue.size();
  }

  private void runNode(TaskNode node) {
    executorService.execute(() -> {
      try {
        INodeTrigger remoteInvoker = remoteInvokerMap.get(node.getExecuteType());
        String result = remoteInvoker.queryStatus(node.getRefreshContext(), node);
        log.info("get query status result={}", result);
        if (StringUtils.isBlank(result)) {
          handleDefaultError(node);
          return;
        }

        if (stopPipelineHistoryMap.containsKey(node.getHistoryId())) {
          log.info("record result ignore , because pipeline is stop historyId={} recordId={}",
              node.getHistoryId(), node.getRecordId());
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
      } catch (Exception e) {
        log.info("loop query status error", e);
        handleDefaultError(node);
      }
    });
  }

  private void handleDefaultError(TaskNode node) {
    QueryResponseModel queryResponse = new QueryResponseModel();
    queryResponse.setStatus(ProcessStatus.FAIL.getType());
    queryResponse.setData(new JSONObject());
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
    if (!Objects.equals(responseModel.getStatus(), ProcessStatus.SUCCESS.getType())) {
      return;
    }

    JSONObject jsonObject = responseModel.getData();
    List<CompareInfo> compareConfigs = node.getRefreshContext().getCompareConfig();
    for (CompareInfo compareInfo : compareConfigs) {
      CompareResult compareResult = handleCompare(jsonObject, compareInfo);
      if (!compareResult.getCompareStatus()) {
        responseModel.setStatus(ProcessStatus.FAIL.getType());
        responseModel.setMessage(exchangeTips(jsonObject, compareInfo));
        return;
      }
    }
  }

  private CompareResult handleCompare(JSONObject jsonObject, CompareInfo compareInfo) {
    CompareOperator compareOperator = compareFactory.getOperator(compareInfo.getOperator());
    CompareDefine compareDefine = new CompareDefine();
    compareDefine.setResponseValue(jsonObject.get(compareInfo.getCompareKey()));
    compareDefine.setExpectValue(compareInfo.getValue());
    return compareOperator.compare(compareDefine);
  }

  private List<String> exchangeTips(JSONObject jsonObject, CompareInfo compareInfo) {
    String desc = String.format(DESCRIPTION_FORMAT, compareInfo.getCompareKey(),
        compareInfo.getDescription());
    String expectDesc = String.format(EXPECT_VALUE_FORMAT, compareInfo.getValue());
    String operatorDesc = String.format(OPERATOR_FORMAT, compareInfo.getOperator());
    String resultDesc = String.format(RESULT_VALUE_FORMAT,
        jsonObject.get(compareInfo.getCompareKey()));
    return Arrays.asList(desc, resultDesc, operatorDesc, expectDesc);
  }

  private static void notifyStatus(TaskNode node, QueryResponseModel responseModel) {
    PipelineStatusEvent statusEvent = PipelineStatusEvent.builder().taskNode(node)
        .processStatus(ProcessStatus.exchange(responseModel.getStatus()))
        .errorMsg(responseModel.getMessage()).build();
    PipelineEventFactory.sendNotifyEvent(statusEvent);
  }

  private void cycleRunTask(TaskNode node) {
    if (checkRunTimeout(node.getExecuteTime())) {
      log.info("node record run timeout recordId={}", node.getRecordId());
      QueryResponseModel queryResponseModel = new QueryResponseModel();
      queryResponseModel.setStatus(ProcessStatus.TIMEOUT.getType());
      queryResponseModel.addMessage(ProcessStatus.TIMEOUT.getDesc());
      handleRecordFinalStatus(node, queryResponseModel);
      putAndCheckRecord(node.getRecordId());
      return;
    }
    queue.add(node);
  }

  /**
   * 任务执行超过最大超时时间退出循环查询
   */
  private boolean checkRunTimeout(long executeTime) {
    long dateNow = System.currentTimeMillis();
    long mills = dateNow - executeTime;
    Integer timeout = globalEnvConfig.getLoopQueryTimeout();
    return mills >= timeout;
  }

  public void run() {
    while (true) {
      TaskNode taskNode = null;
      try {
        taskNode = queue.poll(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        log.info("get task from queue error", e);
      }

      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
      }

      if (Objects.nonNull(taskNode)) {
        runNode(taskNode);
      }
    }
  }

  public void stopPipeline(String historyId) {
    putAndCheckRecord(historyId);
  }

  private void putAndCheckRecord(String historyId) {
    Long dateNow = System.currentTimeMillis();
    stopPipelineHistoryMap.put(historyId, dateNow);
    stopPipelineHistoryMap.entrySet().removeIf(entity -> {
      long mills = entity.getValue() - dateNow;
      return mills > globalEnvConfig.getLoopQueryTimeout();
    });
  }
}
