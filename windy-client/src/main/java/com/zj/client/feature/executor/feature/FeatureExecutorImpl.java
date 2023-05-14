package com.zj.client.feature.executor.feature;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.enuns.ExecuteStatusEnum;
import com.zj.client.entity.po.ExecutePoint;
import com.zj.client.entity.po.ExecuteRecord;
import com.zj.client.entity.po.FeatureHistory;
import com.zj.client.entity.vo.ExecuteDetail;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.feature.executor.feature.strategy.ExecuteStrategyFactory;
import com.zj.client.feature.executor.vo.ExecuteContext;
import com.zj.client.feature.executor.vo.ExecutorUnit;
import com.zj.client.notify.IResultEventNotify;
import com.zj.client.notify.NotifyType;
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
  public void execute(List<ExecutePoint> executePointList, String featureId, String recordId,
      ExecuteContext executeContext) {
    String historyId = uniqueIdService.getUniqueId();
    createFeatureHistory(featureId, historyId, recordId);

    CompletableFuture.runAsync(() -> {
      //1 根据用户的选择先排序执行点
      List<ExecutePoint> executePoints = executePointList.stream()
          .sorted(Comparator.comparing(ExecutePoint::getSortOrder)).collect(Collectors.toList());

      AtomicInteger status = new AtomicInteger(ExecuteStatusEnum.SUCCESS.getStatus());
      for (ExecutePoint executePoint : executePoints) {
        ExecuteRecord executeRecord = new ExecuteRecord();
        try {
          //2 使用策略类执行用例
          List<FeatureResponse> responses = executeStrategyFactory.execute(executePoint,
              executeContext);

          boolean allSuccess = responses.stream().allMatch(FeatureResponse::isSuccess);
          executeRecord.setStatus(allSuccess ? ExecuteStatusEnum.SUCCESS.getStatus()
              : ExecuteStatusEnum.FAILED.getStatus());
          executeRecord.setExecuteResult(JSON.toJSONString(responses));
        } catch (Exception e) {
          log.error("execute error", e);
          FeatureResponse featureResponse = createFailResponse(executePoint, e);
          executeRecord.setStatus(ExecuteStatusEnum.FAILED.getStatus());
          executeRecord.setExecuteResult(
              JSON.toJSONString(Collections.singletonList(featureResponse)));
        }

        //3 保存执行点记录
        saveRecord(historyId, executePoint, executeRecord);
        if (Objects.equals(executeRecord.getStatus(), ExecuteStatusEnum.FAILED.getStatus())) {
          log.warn("execute feature error featureId= {}", executePoint.getFeatureId());
          status.set(ExecuteStatusEnum.FAILED.getStatus());
          break;
        }
      }

      //4 更新整个用例执行结果
      resultEventNotify.notify(historyId, NotifyType.UPDATE_FEATURE_HISTORY, ProcessStatus.exchange(status.get()), null);
    }, executorService);
  }

  private void saveRecord(String historyId, ExecutePoint executePoint,
      ExecuteRecord executeRecord) {
    executeRecord.setHistoryId(historyId);
    executeRecord.setExecutePointId(executePoint.getPointId());
    ExecutorUnit unit = JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit.class);
    executeRecord.setExecutePointName(unit.getName());
    executeRecord.setExecuteType(executePoint.getExecuteType());
    executeRecord.setCreateTime(System.currentTimeMillis());
    executeRecord.setTestStage(executePoint.getTestStage());
    String recordId = uniqueIdService.getUniqueId();
    executeRecord.setExecuteRecordId(recordId);
    resultEventNotify.notify(recordId, NotifyType.CREATE_EXECUTE_POINT_RECORD, ProcessStatus.RUNNING, executeRecord);
  }

  public void createFeatureHistory(String featureId, String historyId, String recordId) {
    FeatureHistory featureHistory = new FeatureHistory();
    featureHistory.setFeatureId(featureId);
    featureHistory.setExecuteStatus(ExecuteStatusEnum.RUNNING.getStatus());
    featureHistory.setHistoryId(historyId);
    featureHistory.setRecordId(recordId);
    featureHistory.setCreateTime(System.currentTimeMillis());
    resultEventNotify.notify(historyId,NotifyType.CREATE_FEATURE_HISTORY,  ProcessStatus.RUNNING, featureHistory);
  }

  private FeatureResponse createFailResponse(ExecutePoint executePoint, Exception e) {
    ExecuteDetail executeDetail = new ExecuteDetail();
    executeDetail.setErrorMessage(e.toString());
    executeDetail.setStatus(false);
    return FeatureResponse.builder()
        .pointId(executePoint.getPointId()).executeDetail(executeDetail).build();
  }
}
