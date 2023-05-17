package com.zj.feature.executor.feature;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.feature.ExecuteRecordDto;
import com.zj.domain.entity.dto.feature.TaskRecordDto;
import com.zj.domain.entity.po.feature.ExecutePoint;
import com.zj.feature.entity.vo.ExecuteDetail;
import com.zj.feature.entity.vo.FeatureConstant;
import com.zj.feature.entity.vo.FeatureResponse;
import com.zj.feature.executor.IFeatureExecutor;
import com.zj.feature.executor.feature.strategy.ExecuteStrategyFactory;
import com.zj.feature.executor.vo.ExecuteContext;
import com.zj.feature.executor.vo.ExecutorUnit;
import com.zj.feature.service.ExecutePointService;
import com.zj.feature.service.ExecuteRecordService;
import com.zj.feature.service.FeatureHistoryService;
import com.zj.feature.service.ICacheService;
import com.zj.feature.service.TaskRecordService;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeatureExecutorImpl implements IFeatureExecutor {

  private final ExecutePointService executePointService;

  private final FeatureHistoryService featureHistoryService;

  private final ExecuteRecordService executeRecordService;

  private final ICacheService cacheService;

  private final TaskRecordService taskRecordService;

  private final ExecuteStrategyFactory executeStrategyFactory;

  private final UniqueIdService uniqueIdService;

  private final ExecutorService executorService = Executors.newFixedThreadPool(30);

  public FeatureExecutorImpl(ExecutePointService executePointService,
      FeatureHistoryService featureHistoryService, ExecuteRecordService executeRecordService,
      ICacheService cacheService, TaskRecordService taskRecordService,
      ExecuteStrategyFactory executeStrategyFactory, UniqueIdService uniqueIdService) {
    this.executePointService = executePointService;
    this.featureHistoryService = featureHistoryService;
    this.executeRecordService = executeRecordService;
    this.cacheService = cacheService;
    this.taskRecordService = taskRecordService;
    this.executeStrategyFactory = executeStrategyFactory;
    this.uniqueIdService = uniqueIdService;
  }

  @Override
  public String execute(String featureId, String recordId, ExecuteContext executeContext) {
    String historyId = uniqueIdService.getUniqueId();
    featureHistoryService.saveFeatureHistory(featureId, historyId, recordId);

    executorService.execute(() -> {
      //1 根据用户的选择先排序执行点
      List<ExecutePoint> executePoints = executePointService.getExecutePointByFeatureIds(
          Collections.singletonList(featureId));
      executePoints = executePoints.stream()
          .sorted(Comparator.comparing(ExecutePoint::getSortOrder)).collect(Collectors.toList());;

      AtomicInteger status = new AtomicInteger(ProcessStatus.SUCCESS.getType());
      for (ExecutePoint executePoint : executePoints) {
        ExecuteRecordDto executeRecord = new ExecuteRecordDto();
        try {
          //2 使用策略类执行用例
          List<FeatureResponse> responses = executeStrategyFactory.execute(executePoint, executeContext);

          boolean allSuccess = responses.stream().allMatch(FeatureResponse::isSuccess);
          executeRecord.setStatus(allSuccess ? ProcessStatus.SUCCESS.getType()
                  : ProcessStatus.FAIL.getType());
          executeRecord.setExecuteResult(JSON.toJSONString(responses));
        } catch (Exception e) {
          log.error("execute error", e);
          FeatureResponse featureResponse = createFailResponse(executePoint, e);
          executeRecord.setStatus(ProcessStatus.FAIL.getType());
          executeRecord.setExecuteResult(JSON.toJSONString(Collections.singletonList(featureResponse)));
        }

        //3 保存执行点记录
        saveRecord2DB(historyId, executePoint, executeRecord);

        if (Objects.equals(executeRecord.getStatus(), ProcessStatus.FAIL.getType())) {
          log.warn("execute feature error featureId= {}", executePoint.getFeatureId());
          status.set(ProcessStatus.FAIL.getType());
          break;
        }
      }

      //4 更新整个用例执行结果
      updateHistoryStatus(historyId, status.get());

      //8 如果是任务执行，还要修改任务状态
      updateTaskExecuteStatus(recordId, featureId, status.get());
    });
    return historyId;
  }

  private void saveRecord2DB(String historyId, ExecutePoint executePoint, ExecuteRecordDto executeRecord) {
    executeRecord.setHistoryId(historyId);
    executeRecord.setExecutePointId(executePoint.getPointId());
    ExecutorUnit unit = JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit.class);
    executeRecord.setExecutePointName(unit.getName());
    executeRecord.setExecuteType(executePoint.getExecuteType());
    executeRecord.setCreateTime(System.currentTimeMillis());
    executeRecord.setTestStage(executePoint.getTestStage());
    executeRecord.setExecuteRecordId(uniqueIdService.getUniqueId());
    executeRecordService.save(executeRecord);
  }

  private void updateTaskExecuteStatus(String recordId, String featureId, Integer status) {
    if (StringUtils.isBlank(recordId)) {
      log.info("recordId is empty not update status recordId={}", recordId);
      return;
    }

    log.info("start update status");
    String cacheKey = FeatureConstant.RECORD_STATUS_CACHE_KEY + recordId;
    String cacheValue = cacheService.getCache(cacheKey);
    JSONObject jsonObject = JSON.parseObject(cacheValue);
    if (Objects.isNull(jsonObject) || Objects.isNull(jsonObject.getInteger(featureId))) {
      log.info("can not find cache not update status recordId={}", recordId);
      return;
    }

    //如果所有的用例都执行完成，则修改记录并且删除缓存
    jsonObject.put(featureId, status);
    boolean allComplete = jsonObject.keySet().stream().noneMatch(
        key -> Objects.equals(jsonObject.getInteger(key), ProcessStatus.RUNNING.getType()));
    if (allComplete) {
      log.info("all features complete clear cache and update status recordId={}", recordId);
      cacheService.deleteCache(cacheKey);
      taskRecordService.updateRecordStatus(recordId, status);
      return;
    }

    String value = JSON.toJSONString(jsonObject);
    log.info("update feature status recordId={} status={}", recordId, value);
    cacheService.setCache(cacheKey, value);
  }

  private FeatureResponse createFailResponse(ExecutePoint executePoint, Exception e) {
    ExecuteDetail executeDetail = new ExecuteDetail();
    executeDetail.setErrorMessage(e.toString());
    executeDetail.setStatus(false);
    return FeatureResponse.builder()
        .pointId(executePoint.getPointId()).executeDetail(executeDetail).build();
  }

  private void updateHistoryStatus(String historyId, int status) {
    log.info("execute result status ={}", status);
    featureHistoryService.updateStatus(historyId, status);
  }

  @Override
  public List<String> batchRunTask(List<String> featureIds, TaskRecordDto taskRecord) {
    ExecuteContext executeContext = buildTaskConfig(taskRecord.getTaskConfig());
    return featureIds.stream()
        .map(featureId -> execute(featureId, taskRecord.getRecordId(), executeContext))
        .collect(Collectors.toList());
  }

  private ExecuteContext buildTaskConfig(String taskConfig) {
    ExecuteContext executeContext = new ExecuteContext();
    if (StringUtils.isBlank(taskConfig)) {
      return executeContext;
    }

    JSONObject jsonObject = JSON.parseObject(taskConfig);
    for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
      executeContext.set(entry.getKey(), entry.getValue());
    }
    return executeContext;
  }
}
