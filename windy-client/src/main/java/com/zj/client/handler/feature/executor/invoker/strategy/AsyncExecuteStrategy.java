package com.zj.client.handler.feature.executor.invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.entity.enuns.ExecutePointType;
import com.zj.client.entity.vo.ExecutePoint;
import com.zj.client.entity.vo.ExecuteRecord;
import com.zj.client.entity.vo.FeatureResponse;
import com.zj.client.handler.feature.executor.FeatureExecutorImpl;
import com.zj.client.handler.feature.executor.compare.CompareHandler;
import com.zj.client.handler.feature.executor.interceptor.InterceptorProxy;
import com.zj.client.handler.feature.executor.invoker.IExecuteInvoker;
import com.zj.client.handler.feature.executor.vo.ExecuteContext;
import com.zj.client.handler.notify.IResultEventNotify;
import com.zj.client.utils.ExceptionUtils;
import com.zj.common.enums.NotifyType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.feature.ExecutePointDto;
import com.zj.common.feature.ExecutorUnit;
import com.zj.common.model.ResultEvent;
import com.zj.common.utils.IpUtils;
import com.zj.plugin.loader.ExecuteDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    public List<ExecutePointType> getType() {
        return Collections.singletonList(ExecutePointType.TREAD);
    }

    @Override
    public List<FeatureResponse> execute(ExecutePoint executePoint, ExecuteContext executeContext) {
        log.info("start execute AsyncExecuteStrategy context={}", JSON.toJSONString(executeContext.toMap()));
        ExecutorUnit executorUnit = JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit.class);
        String timeout = executorUnit.getMethod();
        executor.execute(() ->{
            CountDownLatch countDownLatch = new CountDownLatch(1);
            CompletableFuture.runAsync(() -> {
                List<ExecutePointDto> executePoints = executorUnit.getExecutePoints();
                List<FeatureResponse> responses = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(executePoints)){
                    responses = executePoints.stream().map(executePointDto -> {
                        ExecutePoint point = toExecutePoint(executePointDto);
                        return executeFeature(executeContext, point);
                    }).collect(Collectors.toList());
                }
                countDownLatch.countDown();
                notifyAsyncRecordResult(executeContext, responses);
            }, executor).exceptionally(throwable -> {
                log.info("AsyncExecuteStrategy feature execute error", throwable);
                notifyError(executeContext, ExceptionUtils.getSimplifyError(throwable), timeout);
                return null;
            });

            try {
                boolean result = countDownLatch.await(Integer.parseInt(timeout), TimeUnit.SECONDS);
                log.info("wait time out result= {}", result);
            } catch (InterruptedException e) {
                notifyError(executeContext, "执行任务超时", timeout);
            }
        });


        ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
        executeDetailVo.addRequestInfo("timeout", timeout);
        executeDetailVo.setErrorMessage("异步任务执行中");
        executeDetailVo.getResponseDetailVo().setProcessStatus(ProcessStatus.RUNNING.getType());
        FeatureResponse featureResponse = new FeatureResponse();
        featureResponse.setExecuteDetailVo(executeDetailVo);
        return Collections.singletonList(featureResponse);
    }

    private void notifyError(ExecuteContext executeContext, String errorMessage, String timeout) {
        ExecuteDetailVo executeDetailVo = new ExecuteDetailVo();
        executeDetailVo.addRequestInfo("timeout", timeout);
        executeDetailVo.setErrorMessage(errorMessage);
        executeDetailVo.setStatus(false);
        FeatureResponse featureResponse = new FeatureResponse();
        featureResponse.setExecuteDetailVo(executeDetailVo);
        notifyAsyncRecordResult(executeContext, Collections.singletonList(featureResponse));
    }

    private void notifyAsyncRecordResult(ExecuteContext executeContext, List<FeatureResponse> responses) {
        ExecuteRecord executeRecord = new ExecuteRecord();
        executeRecord.setHistoryId(executeContext.getHistoryId());
        executeRecord.setExecuteRecordId(executeContext.getRecordId());
        executeRecord.setExecuteResult(JSON.toJSONString(responses));
        executeRecord.setStatus(FeatureExecutorImpl.judgeRecordStatus(responses));
        ResultEvent resultEvent = new ResultEvent().executeId(executeContext.getRecordId())
                .notifyType(NotifyType.UPDATE_EXECUTE_POINT_RECORD)
                .clientIp(IpUtils.getLocalIP())
                .logId(executeContext.getLogId())
                .params(executeRecord);
        resultEventNotify.notifyEvent(resultEvent);
    }
}
