package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.vo.TestFeatureParamVo;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TriggerContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.handler.pipeline.executer.vo.TestRequestContext;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ExecuteException;
import com.zj.common.monitor.RequestProxy;
import com.zj.common.utils.OrikaUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * 用例执行处理
 * @author guyuelan
 * @since 2023/5/9
 */
@Slf4j
@Component
public class FeatureTrigger implements INodeTrigger {
  private static final String TASK_STATUS_URL = "http://WindyMaster/v1/devops/master/task/%s/status";
  public static final String TASK_TIPS = "pipeline feature task";

  private RequestProxy requestProxy;

  public FeatureTrigger(RequestProxy requestProxy) {
    this.requestProxy = requestProxy;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.TEST;
  }

  @Override
  public void triggerRun(TriggerContext triggerContext, TaskNode taskNode) {
    TestRequestContext context = OrikaUtil.convert(triggerContext.getData(),
        TestRequestContext.class);
    String taskId = context.getTaskId();
    TestFeatureParamVo paramVo = new TestFeatureParamVo();
    paramVo.setSourceId(taskId);
    paramVo.setSourceName(TASK_TIPS);
    paramVo.setType(LogType.FEATURE_TASK.getType());

    ResponseEntity<JSONObject> responseEntity = requestProxy.startFeatureTask(paramVo);
    JSONObject jsonObject = responseEntity.getBody();
    log.info("get TestFeatureInvoker triggerRun code= {} result={}",
        responseEntity.getStatusCode(), JSON.toJSONString(jsonObject));

    //触发任务执行，将任务的记录Id传递给刷新动作
    if (responseEntity.getStatusCode().is2xxSuccessful()) {
      throw new ExecuteException(JSON.toJSONString(jsonObject));
    }
    String url = String.format(TASK_STATUS_URL, jsonObject.getString("data"));
    triggerContext.getTaskNode().getRefreshContext().setUrl(url);
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    QueryResponseModel queryResponseModel = new QueryResponseModel();
    try {
      log.info("get refresh url ={}", refreshContext.getUrl());
      ResponseEntity<JSONObject> responseEntity = requestProxy.getFeatureTaskStatus(
          refreshContext.getUrl());
      log.info("get TestFeatureInvoker queryStatus code= {} result={}",
          responseEntity.getStatusCode(), JSON.toJSONString(responseEntity.getBody()));
      if (responseEntity.getStatusCode().isError()) {
        queryResponseModel.setStatus(ProcessStatus.FAIL.getType());
        queryResponseModel.setMessage(Collections.singletonList("request http error"));
        return JSON.toJSONString(queryResponseModel);
      }

      return JSON.toJSONString(responseEntity.getBody());
    } catch (Exception e) {
      log.error("request dispatch task error", e);
      queryResponseModel.setStatus(ProcessStatus.FAIL.getType());
      queryResponseModel.setMessage(getErrorMsg(e));
    }
    return JSON.toJSONString(queryResponseModel);
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
