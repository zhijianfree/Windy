package com.zj.pipeline.executer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.pipeline.executer.vo.RefreshContext;
import com.zj.pipeline.executer.vo.TaskNode;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/3/30
 */
@Slf4j
@Component
public class NodeStatusQueryLooper implements IStatusNotifyListener {

  private ExecutorService executorService = new ThreadPoolExecutor(20, 60, 10, TimeUnit.MINUTES,
      new LinkedBlockingQueue<>(1000), new CallerRunsPolicy());

  @Autowired
  private IRemoteInvoker remoteInvoker;
  public void addQuestTask(TaskNode node) {
    executorService.execute(() ->{
      String result = remoteInvoker.queryStatus(node.getRefreshContext(), node.getRecordId());
      JSONObject jsonObject = JSON.parseObject(result);
    });
  }

  @Override
  public void statusChange(PipelineStatusEvent event) {
    log.info("receive pipeline notify={}", JSON.toJSONString(event));
  }
}
