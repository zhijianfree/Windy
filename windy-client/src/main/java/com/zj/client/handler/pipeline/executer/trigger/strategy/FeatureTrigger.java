package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.handler.pipeline.executer.vo.TestRequestContext;
import com.zj.client.handler.pipeline.executer.vo.TriggerContext;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ExecuteException;
import com.zj.common.entity.dto.DispatchTaskModel;
import com.zj.common.entity.dto.ResponseStatusModel;
import com.zj.common.adapter.invoker.IMasterInvoker;
import com.zj.common.utils.OrikaUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 用例执行处理
 * @author guyuelan
 * @since 2023/5/9
 */
@Slf4j
@Component
public class FeatureTrigger implements INodeTrigger {
  public static final String TASK_TIPS = "pipeline feature task";
  private final IMasterInvoker masterInvoker;
  public FeatureTrigger(IMasterInvoker masterInvoker) {
    this.masterInvoker = masterInvoker;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.TEST;
  }

  @Override
  public void triggerRun(TriggerContext triggerContext, TaskNode taskNode) {
    log.info("start run feature context={}", JSON.toJSONString(triggerContext));
    TestRequestContext context = OrikaUtil.convert(triggerContext.getData(),
        TestRequestContext.class);
    String taskId = context.getTaskId();
    DispatchTaskModel task = new DispatchTaskModel();
    task.setSourceId(taskId);
    task.setSourceName(TASK_TIPS);
    task.setType(LogType.FEATURE_TASK.getType());
    task.setTriggerId(taskNode.getRecordId());
    String recordId = masterInvoker.runFeatureTask(task);
    log.info("get TestFeatureInvoker triggerRun recordId= {}",recordId);

    //触发任务执行，将任务的记录Id传递给刷新动作
    if (StringUtils.isBlank(recordId)) {
      throw new ExecuteException("pipeline trigger feature task error");
    }
    taskNode.getRefreshContext().setRecordId(recordId);
  }

  @Override
  public QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    QueryResponseModel queryResponseModel = new QueryResponseModel();
    try {
      log.info("get refresh url ={}", refreshContext.getUrl());
      ResponseStatusModel responseStatusModel = masterInvoker.getFeatureTaskStatus(refreshContext.getRecordId());
      log.info("get TestFeatureInvoker result={}", JSON.toJSONString(responseStatusModel));
      if (Objects.isNull(responseStatusModel)) {
        queryResponseModel.setStatus(ProcessStatus.FAIL.getType());
        queryResponseModel.setMessage(Collections.singletonList("request http error"));
        return queryResponseModel;
      }

      return JSON.parseObject(JSON.toJSONString(responseStatusModel), QueryResponseModel.class);
    } catch (Exception e) {
      log.error("request dispatch task error", e);
      queryResponseModel.setStatus(ProcessStatus.FAIL.getType());
      queryResponseModel.setMessage(getErrorMsg(e));
    }
    return queryResponseModel;
  }

  private List<String> getErrorMsg(Exception exception) {
    List<String> msg = new ArrayList<>();
    msg.add("trigger node task error: " + exception.toString());
    for (StackTraceElement element : exception.getStackTrace()) {
      msg.add(element.toString());
    }
    return msg;
  }
}
