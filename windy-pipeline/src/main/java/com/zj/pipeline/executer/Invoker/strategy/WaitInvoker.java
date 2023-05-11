package com.zj.pipeline.executer.Invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ProcessStatus;
import com.zj.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.pipeline.executer.vo.ExecuteType;
import com.zj.pipeline.executer.vo.QueryResponseModel;
import com.zj.pipeline.executer.vo.RefreshContext;
import com.zj.pipeline.executer.vo.RequestContext;
import com.zj.pipeline.executer.vo.WaitRequestContext;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class WaitInvoker implements IRemoteInvoker {

  public static final String MESSAGE_TIPS = "等待执行结果";
  private final ConcurrentHashMap<String, CountDownLatch> countDownMap = new ConcurrentHashMap<>();

  @Override
  public ExecuteType type() {
    return ExecuteType.WAIT;
  }

  @Override
  public boolean triggerRun(RequestContext requestContext, String recordId) throws IOException {
    WaitRequestContext waitRequestContext = (WaitRequestContext) requestContext;
    CompletableFuture.runAsync(() -> {
      try {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownMap.put(recordId, countDownLatch);
        countDownLatch.await(waitRequestContext.getWaitTime(), TimeUnit.SECONDS);
      } catch (InterruptedException ignore) {
      }
    }).whenComplete((consumer,ex) ->{
      CountDownLatch countDownLatch = countDownMap.get(recordId);
      countDownLatch.countDown();
    });
    return true;

  }

  @Override
  public String queryStatus(RefreshContext refreshContext, String recordId) {
    CountDownLatch countDownLatch = countDownMap.get(recordId);
    long count = countDownLatch.getCount();
    QueryResponseModel responseModel = new QueryResponseModel();
    responseModel.setMessage(Collections.singletonList(MESSAGE_TIPS));
    responseModel.setStatus(ProcessStatus.RUNNING.getType());
    if (count > 0) {
      return JSON.toJSONString(responseModel);
    }

    log.info("wait task complete recordId={}", recordId);
    responseModel.setStatus(ProcessStatus.SUCCESS.getType());
    return JSON.toJSONString(responseModel);
  }
}
