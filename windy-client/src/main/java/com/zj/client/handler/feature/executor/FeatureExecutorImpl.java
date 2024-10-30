package com.zj.client.handler.feature.executor;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.ExecuteRecord;
import com.zj.client.entity.vo.FeatureHistory;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.handler.feature.executor.invoker.strategy.ExecuteStrategyFactory;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.client.handler.feature.executor.vo.FeatureParam;
import com.zj.client.handler.notify.IResultEventNotify;
import com.zj.client.utils.ExceptionUtils;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.enums.TemplateType;
import com.zj.common.model.ResultEvent;
import com.zj.common.utils.IpUtils;
import com.zj.common.uuid.UniqueIdService;
import com.zj.plugin.loader.ExecuteDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FeatureExecutorImpl implements IFeatureExecutor {

    private final ExecuteStrategyFactory executeStrategyFactory;

    private final UniqueIdService uniqueIdService;

    private final IResultEventNotify resultEventNotify;

    @Autowired
    @Qualifier("featureExecutePool")
    private Executor executor;

    public FeatureExecutorImpl(
            ExecuteStrategyFactory executeStrategyFactory, UniqueIdService uniqueIdService,
            IResultEventNotify resultEventNotify) {
        this.executeStrategyFactory = executeStrategyFactory;
        this.uniqueIdService = uniqueIdService;
        this.resultEventNotify = resultEventNotify;
    }

    @Override
    public void execute(FeatureParam featureParam) {
        log.info("execute get token ={}", featureParam.getExecuteContext().get("accessToken"));
        String historyId = uniqueIdService.getUniqueId();
        FeatureHistory featureHistory = saveFeatureHistory(featureParam.getFeatureId(), historyId,
                featureParam.getTaskRecordId());

        CompletableFuture.runAsync(() -> {
            //1 根据用户的选择先排序执行点
            List<ExecutePoint> executePoints = featureParam.getExecutePointList().stream()
                    .sorted(Comparator.comparing(ExecutePoint::getSortOrder)).collect(Collectors.toList());

            AtomicInteger status = new AtomicInteger(ProcessStatus.SUCCESS.getType());
            FeatureExecuteContext featureExecuteContext = new FeatureExecuteContext();
            featureExecuteContext.bindMap(featureParam.getExecuteContext());
            featureExecuteContext.setHistoryId(historyId);
            Map<String, Object> globalContext = new HashMap<>();
            CountDownLatch countDownLatch = null;
            for (ExecutePoint executePoint : executePoints) {
                judgeWhetherWait(countDownLatch);
                ExecuteRecord executeRecord = new ExecuteRecord();
                String recordId = uniqueIdService.getUniqueId();
                executeRecord.setExecuteRecordId(recordId);
                executeRecord.setHistoryId(historyId);
                try {
                    //2 使用策略类执行用例
                    featureExecuteContext.setRecordId(recordId);
                    featureExecuteContext.setLogId(featureParam.getLogId());

                    //如果是异步执行模版就需要添加countDown，保证后续任务是串行执行
                    if (Objects.equals(executePoint.getExecuteType(), TemplateType.THREAD.getType())) {
                        countDownLatch = new CountDownLatch(1);
                        featureExecuteContext.setCountDownLatch(countDownLatch);
                    }
                    List<FeatureResponse> responses = executeStrategyFactory.execute(executePoint, featureExecuteContext);
                    executeRecord.setStatus(judgeRecordStatus(responses));
                    executeRecord.setExecuteResult(JSON.toJSONString(responses));

                    Map<String, Object> pointGlobalContext =
                            responses.stream().map(FeatureResponse::getContext).filter(Objects::nonNull).flatMap(map -> map.entrySet().stream())
                                    .filter(entry -> Objects.nonNull(entry.getValue()))
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    globalContext.putAll(pointGlobalContext);
                } catch (Exception e) {
                    log.error("execute error", e);
                    FeatureResponse featureResponse = createFailResponse(executePoint, e);
                    executeRecord.setStatus(ProcessStatus.FAIL.getType());
                    executeRecord.setExecuteResult(
                            JSON.toJSONString(Collections.singletonList(featureResponse)));
                }

                //3 保存执行点记录
                saveRecord(featureParam, executePoint, executeRecord);
                if (Objects.equals(executeRecord.getStatus(), ProcessStatus.FAIL.getType())) {
                    log.warn("execute feature error featureId= {}", executePoint.getFeatureId());
                    status.set(executeRecord.getStatus());
                    break;
                }
            }

            //4 更新整个用例执行结果
            featureHistory.setExecuteStatus(status.get());
            ResultEvent resultEvent = new ResultEvent().executeId(featureParam.getTaskRecordId())
                    .notifyType(NotifyType.UPDATE_FEATURE_HISTORY)
                    .masterIP(featureParam.getMasterIp())
                    .logId(featureParam.getLogId())
                    .status(ProcessStatus.exchange(status.get()))
                    .context(globalContext)
                    .params(featureHistory);
            resultEventNotify.notifyEvent(resultEvent);
        }, executor);
    }

    /**
     * 上个任务是异步任务的需要等上个任务执行之后再开始下一个避免
     */
    private  void judgeWhetherWait(CountDownLatch countDownLatch) {
        if (Objects.isNull(countDownLatch) || countDownLatch.getCount() < 1) {
            return;
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException ignore) {
            log.info("async task not trigger,continue execute next task");
        }
    }

    public static Integer judgeRecordStatus(List<FeatureResponse> responses) {
        boolean processing = responses.stream().allMatch(FeatureResponse::isProcessing);
        if (processing) {
            return ProcessStatus.RUNNING.getType();
        }
        boolean allSuccess = responses.stream().allMatch(FeatureResponse::isSuccess);
        return allSuccess ? ProcessStatus.SUCCESS.getType() : ProcessStatus.FAIL.getType();
    }

    private void saveRecord(FeatureParam featureParam, ExecutePoint executePoint,
                            ExecuteRecord executeRecord) {
        executeRecord.setExecutePointId(executePoint.getPointId());
        executeRecord.setExecutePointName(executePoint.getDescription());
        executeRecord.setExecuteType(executePoint.getExecuteType());
        executeRecord.setCreateTime(System.currentTimeMillis());
        executeRecord.setTestStage(executePoint.getTestStage());
        ResultEvent resultEvent = new ResultEvent().executeId(executeRecord.getExecuteRecordId())
                .notifyType(NotifyType.CREATE_EXECUTE_POINT_RECORD)
                .masterIP(featureParam.getMasterIp())
                .clientIp(IpUtils.getLocalIP())
                .logId(featureParam.getLogId())
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
        ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
        executeDetailVo.setErrorMessage(ExceptionUtils.getSimplifyError(e));
        executeDetailVo.setStatus(false);
        FeatureResponse featureResponse = new FeatureResponse();
        featureResponse.setPointId(executePoint.getPointId());
        featureResponse.setExecuteDetailVo(executeDetailVo);
        return featureResponse;
    }
}
