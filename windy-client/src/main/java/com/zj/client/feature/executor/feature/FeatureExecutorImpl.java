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
import com.zj.client.feature.executor.vo.ExecuteResult;
import com.zj.client.feature.executor.vo.ExecutorUnit;
import com.zj.common.generate.UniqueIdService;
import java.util.ArrayList;
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

  private final ExecutorService executorService = Executors.newFixedThreadPool(30);

  public FeatureExecutorImpl(
      ExecuteStrategyFactory executeStrategyFactory, UniqueIdService uniqueIdService) {
    this.executeStrategyFactory = executeStrategyFactory;
    this.uniqueIdService = uniqueIdService;
  }

  @Override
  public CompletableFuture<ExecuteResult> execute(List<ExecutePoint> executePointList, String featureId, String recordId,
      ExecuteContext executeContext) {
    String historyId = uniqueIdService.getUniqueId();
    FeatureHistory featureHistory = createFeatureHistory(featureId, historyId, recordId);

    return CompletableFuture.supplyAsync(() -> {
      //1 根据用户的选择先排序执行点
      List<ExecutePoint> executePoints = executePointList.stream()
          .sorted(Comparator.comparing(ExecutePoint::getSortOrder)).collect(Collectors.toList());

      AtomicInteger status = new AtomicInteger(ExecuteStatusEnum.SUCCESS.getStatus());
      List<ExecuteRecord> executeRecords = new ArrayList<>();
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
        executeRecords.add(executeRecord);
        if (Objects.equals(executeRecord.getStatus(), ExecuteStatusEnum.FAILED.getStatus())) {
          log.warn("execute feature error featureId= {}", executePoint.getFeatureId());
          status.set(ExecuteStatusEnum.FAILED.getStatus());
          break;
        }
      }

      //4 更新整个用例执行结果
      featureHistory.setExecuteStatus(status.get());
      return new ExecuteResult(featureHistory, executeRecords);
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
    executeRecord.setExecuteRecordId(uniqueIdService.getUniqueId());
  }

  public FeatureHistory createFeatureHistory(String featureId, String historyId, String recordId) {
    FeatureHistory featureHistory = new FeatureHistory();
    featureHistory.setFeatureId(featureId);
    featureHistory.setExecuteStatus(ExecuteStatusEnum.RUNNING.getStatus());
    featureHistory.setHistoryId(historyId);
    featureHistory.setRecordId(recordId);
    featureHistory.setCreateTime(System.currentTimeMillis());
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
