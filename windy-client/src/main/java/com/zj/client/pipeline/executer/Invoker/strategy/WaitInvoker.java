package com.zj.client.pipeline.executer.Invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.client.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.RequestContext;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.client.pipeline.executer.vo.WaitRequestContext;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.OrikaUtil;
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
  public boolean triggerRun(RequestContext requestContext, TaskNode taskNode) throws IOException {
    WaitRequestContext waitRequestContext = JSON.parseObject(
        JSON.toJSONString(requestContext.getData()), WaitRequestContext.class);
    CompletableFuture.runAsync(() -> {
      try {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownMap.put(taskNode.getRecordId(), countDownLatch);
        countDownLatch.await(waitRequestContext.getWaitTime(), TimeUnit.SECONDS);
      } catch (InterruptedException ignore) {
      }
    }).whenComplete((consumer,ex) ->{
      CountDownLatch countDownLatch = countDownMap.get(taskNode.getRecordId());
      countDownLatch.countDown();
    }).exceptionally(ex ->{
      log.error("run wait error", ex);
      return null;
    });
    return true;

  }

  @Override
  public String queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    CountDownLatch countDownLatch = countDownMap.get(taskNode.getRecordId());
    long count = countDownLatch.getCount();
    QueryResponseModel responseModel = new QueryResponseModel();
    responseModel.setMessage(Collections.singletonList(MESSAGE_TIPS));
    responseModel.setStatus(ProcessStatus.RUNNING.getType());
    if (count > 0) {
      return JSON.toJSONString(responseModel);
    }

    log.info("wait task complete recordId={}", taskNode.getRecordId());
    responseModel.setStatus(ProcessStatus.SUCCESS.getType());
    return JSON.toJSONString(responseModel);
  }
}
