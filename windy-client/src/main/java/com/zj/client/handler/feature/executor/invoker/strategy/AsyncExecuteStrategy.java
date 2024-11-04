package com.zj.client.handler.feature.executor.invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.ExecuteRecord;
import com.zj.common.entity.feature.FeatureResponse;
import com.zj.client.handler.feature.executor.FeatureExecutorImpl;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.vo.FeatureExecuteContext;
import com.zj.client.handler.notify.IResultEventNotify;
import com.zj.client.utils.ExceptionUtils;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.enums.TemplateType;
import com.zj.common.entity.feature.ExecutePointDto;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.common.entity.dto.ResultEvent;
import com.zj.common.utils.IpUtils;
import com.zj.common.utils.OrikaUtil;
import com.zj.plugin.loader.ExecuteDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AsyncExecuteStrategy extends BaseExecuteStrategy {

    private final Executor executor;
    private final IResultEventNotify resultEventNotify;

    protected AsyncExecuteStrategy(InterceptorProxy interceptorProxy, List<IExecuteInvoker> executeInvokers,
                                   CompareHandler compareHandler, IResultEventNotify resultEventNotify,  @Qualifier(
                                           "asyncExecuteFeaturePool") Executor executor) {
        super(interceptorProxy, executeInvokers, compareHandler);
        this.resultEventNotify = resultEventNotify;
        this.executor = executor;
    }


    @Override
    public List<TemplateType> getType() {
        return Collections.singletonList(TemplateType.THREAD);
    }

    @Override
    public List<FeatureResponse> execute(ExecutePoint executePoint, FeatureExecuteContext featureExecuteContext) {
        Map<String, Object> currentContext = featureExecuteContext.toMap();
        log.info("start execute AsyncExecuteStrategy context={}", JSON.toJSONString(currentContext));
        ExecutorUnit executorUnit = JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit.class);
        String timeout = executorUnit.getMethod();
        List<ExecutePointDto> executePoints = executorUnit.getExecutePoints();
        FeatureExecuteContext newContext = OrikaUtil.convert(featureExecuteContext, FeatureExecuteContext.class);
        executor.execute(() ->{
            CountDownLatch countDownLatch = new CountDownLatch(1);
            CompletableFuture.runAsync(() -> {
                if (Objects.nonNull(featureExecuteContext.getCountDownLatch())) {
                    log.info("find count down release wait");
                    featureExecuteContext.getCountDownLatch().countDown();
                }
                List<FeatureResponse> responses = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(executePoints)){
                    responses = executePoints.stream().map(executePointDto -> {
                        try {
                            ExecutePoint point = toExecutePoint(executePointDto);
                            return executeFeature(newContext, point);
                        } catch (Exception e) {
                            log.info("thread execute error", e);
                            ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
                            executeDetailVo.setErrorMessage(ExceptionUtils.getSimplifyError(e));
                            executeDetailVo.getResponseDetailVo().setProcessStatus(ProcessStatus.FAIL.getType());
                            FeatureResponse featureResponse = new FeatureResponse();
                            featureResponse.setExecuteDetailVo(executeDetailVo);
                            featureResponse.setStatus(ProcessStatus.FAIL.getType());
                            featureResponse.setName(executePointDto.getExecutorUnit().getName());
                            return featureResponse;
                        }
                    }).collect(Collectors.toList());
                }
                countDownLatch.countDown();
                notifyAsyncRecordResult(newContext, responses);
            }, executor).exceptionally(throwable -> {
                log.info("AsyncExecuteStrategy feature execute error", throwable);
                countDownLatch.countDown();
                notifyError(newContext, ExceptionUtils.getSimplifyError(throwable), timeout);
                return null;
            });

            try {
                boolean result = countDownLatch.await(Integer.parseInt(timeout), TimeUnit.SECONDS);
                log.info("wait time out result= {}", result);
            } catch (InterruptedException e) {
                notifyError(newContext, "执行任务超时", timeout);
            }
        });

        return executePoints.stream().map(point -> {
            ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
            executeDetailVo.addRequestInfo("timeout", timeout);
            executeDetailVo.setErrorMessage("异步任务执行中");
            executeDetailVo.getResponseDetailVo().setProcessStatus(ProcessStatus.RUNNING.getType());
            FeatureResponse featureResponse = new FeatureResponse();
            featureResponse.setExecuteDetailVo(executeDetailVo);
            featureResponse.setStatus(ProcessStatus.RUNNING.getType());
            featureResponse.setName(point.getExecutorUnit().getName());
            return featureResponse;
        }).collect(Collectors.toList());
    }

    private void notifyError(FeatureExecuteContext featureExecuteContext, String errorMessage, String timeout) {
        ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
        executeDetailVo.addRequestInfo("timeout", timeout);
        executeDetailVo.setErrorMessage(errorMessage);
        executeDetailVo.setStatus(false);
        FeatureResponse featureResponse = new FeatureResponse();
        featureResponse.setExecuteDetailVo(executeDetailVo);
        featureResponse.setStatus(ProcessStatus.FAIL.getType());
        notifyAsyncRecordResult(featureExecuteContext, Collections.singletonList(featureResponse));
    }

    private void notifyAsyncRecordResult(FeatureExecuteContext featureExecuteContext, List<FeatureResponse> responses) {
        ExecuteRecord executeRecord = new ExecuteRecord();
        executeRecord.setHistoryId(featureExecuteContext.getHistoryId());
        executeRecord.setExecuteType(TemplateType.THREAD.getType());
        executeRecord.setExecuteRecordId(featureExecuteContext.getRecordId());
        executeRecord.setRecordResult(responses);
        executeRecord.setStatus(FeatureExecutorImpl.judgeRecordStatus(responses));
        ResultEvent resultEvent = new ResultEvent().executeId(featureExecuteContext.getRecordId())
                .notifyType(NotifyType.UPDATE_EXECUTE_POINT_RECORD)
                .clientIp(IpUtils.getLocalIP())
                .logId(featureExecuteContext.getLogId())
                .params(executeRecord);
        resultEventNotify.notifyEvent(resultEvent);
    }
}
