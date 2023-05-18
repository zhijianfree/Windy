package com.zj.client.feature.executor;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.po.ExecutePoint;
import com.zj.client.entity.po.ExecuteRecord;
import com.zj.client.entity.po.FeatureHistory;
import com.zj.client.entity.vo.ExecuteDetail;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.feature.executor.feature.strategy.ExecuteStrategyFactory;
import com.zj.client.feature.executor.vo.ExecutorUnit;
import com.zj.client.feature.executor.vo.FeatureParam;
import com.zj.client.notify.IResultEventNotify;
import com.zj.common.enums.NotifyType;
import com.zj.common.model.ResultEvent;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeatureExecutorImpl implements IFeatureExecutor {

  private final ExecuteStrategyFactory executeStrategyFactory;

  private final UniqueIdService uniqueIdService;

  private final IResultEventNotify resultEventNotify;

  private final ExecutorService executorService = Executors.newFixedThreadPool(30);

  public FeatureExecutorImpl(
      ExecuteStrategyFactory executeStrategyFactory, UniqueIdService uniqueIdService,
      IResultEventNotify resultEventNotify) {
    this.executeStrategyFactory = executeStrategyFactory;
    this.uniqueIdService = uniqueIdService;
    this.resultEventNotify = resultEventNotify;
  }

  @Override
  public void execute(FeatureParam featureParam) {
    String historyId = uniqueIdService.getUniqueId();
    FeatureHistory featureHistory = saveFeatureHistory(featureParam.getFeatureId(), historyId,
        featureParam.getTaskRecordId());

    CompletableFuture.runAsync(() -> {
      //1 根据用户的选择先排序执行点
      List<ExecutePoint> executePoints = featureParam.getExecutePointList().stream()
          .sorted(Comparator.comparing(ExecutePoint::getSortOrder)).collect(Collectors.toList());

      AtomicInteger status = new AtomicInteger(ProcessStatus.SUCCESS.getType());
      for (ExecutePoint executePoint : executePoints) {
        ExecuteRecord executeRecord = new ExecuteRecord();
        executeRecord.setHistoryId(historyId);
        try {
          //2 使用策略类执行用例
          List<FeatureResponse> responses = executeStrategyFactory.execute(executePoint,
              featureParam.getExecuteContext());

          boolean allSuccess = responses.stream().allMatch(FeatureResponse::isSuccess);
          executeRecord.setStatus(allSuccess ? ProcessStatus.SUCCESS.getType()
              : ProcessStatus.FAIL.getType());
          executeRecord.setExecuteResult(JSON.toJSONString(responses));
        } catch (Exception e) {
          log.error("execute error", e);
          FeatureResponse featureResponse = createFailResponse(executePoint, e);
          executeRecord.setStatus(ProcessStatus.FAIL.getType());
          executeRecord.setExecuteResult(
              JSON.toJSONString(Collections.singletonList(featureResponse)));
        }

        //3 保存执行点记录
        saveRecord(featureParam.getMasterIp(), executePoint, executeRecord);
        if (Objects.equals(executeRecord.getStatus(), ProcessStatus.FAIL.getType())) {
          log.warn("execute feature error featureId= {}", executePoint.getFeatureId());
          status.set(executeRecord.getStatus());
          break;
        }
      }

      featureHistory.setExecuteStatus(status.get());
      //4 更新整个用例执行结果
      ResultEvent resultEvent = new ResultEvent().executeId(featureParam.getTaskRecordId())
          .notifyType(NotifyType.UPDATE_FEATURE_HISTORY)
          .masterIP(featureParam.getMasterIp())
          .status(ProcessStatus.exchange(status.get()))
          .params(featureHistory);
      resultEventNotify.notifyEvent(resultEvent);
    }, executorService);
  }

  private void saveRecord(String masterIp, ExecutePoint executePoint,
      ExecuteRecord executeRecord) {
    executeRecord.setExecutePointId(executePoint.getPointId());
    ExecutorUnit unit = JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit.class);
    executeRecord.setExecutePointName(unit.getName());
    executeRecord.setExecuteType(executePoint.getExecuteType());
    executeRecord.setCreateTime(System.currentTimeMillis());
    executeRecord.setTestStage(executePoint.getTestStage());
    String recordId = uniqueIdService.getUniqueId();
    executeRecord.setExecuteRecordId(recordId);

    ResultEvent resultEvent = new ResultEvent().executeId(recordId)
        .notifyType(NotifyType.CREATE_EXECUTE_POINT_RECORD)
        .masterIP(masterIp)
        .status(ProcessStatus.exchange(executeRecord.getStatus())).params(executeRecord);
    resultEventNotify.notifyEvent(resultEvent);
  }

  public FeatureHistory saveFeatureHistory(String featureId, String historyId, String taskId) {
    FeatureHistory featureHistory = new FeatureHistory();
    featureHistory.setFeatureId(featureId);
    featureHistory.setExecuteStatus(ProcessStatus.RUNNING.getType());
    featureHistory.setHistoryId(historyId);
    featureHistory.setRecordId(taskId);
    featureHistory.setCreateTime(System.currentTimeMillis());

    ResultEvent resultEvent = new ResultEvent().executeId(historyId)
        .notifyType(NotifyType.CREATE_FEATURE_HISTORY)
        .status(ProcessStatus.RUNNING).params(featureHistory);
    resultEventNotify.notifyEvent(resultEvent);
    return featureHistory;
  }

  private FeatureResponse createFailResponse(ExecutePoint executePoint, Exception e) {
    ExecuteDetail executeDetail = new ExecuteDetail();
    executeDetail.setErrorMessage(e.toString());
    executeDetail.setStatus(false);
    return FeatureResponse.builder()
        .pointId(executePoint.getPointId()).executeDetail(executeDetail).build();
  }
}
