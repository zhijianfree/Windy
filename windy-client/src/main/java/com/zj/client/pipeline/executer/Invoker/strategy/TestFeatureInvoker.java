package com.zj.client.pipeline.executer.Invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.vo.TestFeatureParamVo;
import com.zj.client.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.client.pipeline.executer.vo.ExecuteType;
import com.zj.client.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.RequestContext;
import com.zj.client.pipeline.executer.vo.TestRequestContext;
import com.zj.common.enums.LogType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.utils.OrikaUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@Slf4j
@Component
public class TestFeatureInvoker implements IRemoteInvoker {

  public static final String START_TASK_URL = "http://WindyMaster/v1/devops/dispatch/task";

  private static final String TASK_STATUS_URL = "http://WindyMaster/v1/devops/master/task/%s/status";
  public static final String TASK_TIPS = "pipeline feature task";

  @Autowired
  private RestTemplate restTemplate;

  @Override
  public ExecuteType type() {
    return ExecuteType.TEST;
  }

  @Override
  public boolean triggerRun(RequestContext requestContext, String recordId) {
    TestRequestContext context = OrikaUtil.convert(requestContext.getData(),
        TestRequestContext.class);
    String taskId = context.getTaskId();
    TestFeatureParamVo paramVo = new TestFeatureParamVo();
    paramVo.setSourceId(taskId);
    paramVo.setSourceName(TASK_TIPS);
    paramVo.setType(LogType.FEATURE_TASK.getType());

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<TestFeatureParamVo> httpEntity = new HttpEntity<>(paramVo, headers);
    try {
      ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(START_TASK_URL, httpEntity,
          JSONObject.class);

      JSONObject jsonObject = responseEntity.getBody();
      log.info("get TestFeatureInvoker triggerRun code= {} result={}",
          responseEntity.getStatusCode(), JSON.toJSONString(jsonObject));

      //触发任务执行，将任务的记录Id传递给刷新动作
      if (responseEntity.getStatusCode().is2xxSuccessful()) {
        String url = String.format(TASK_STATUS_URL, jsonObject.getString("data"));
        requestContext.getTaskNode().getRefreshContext().setUrl(url);
      }
      return responseEntity.getStatusCode().is2xxSuccessful();
    } catch (Exception e) {
      log.error("request dispatch task error", e);
    }
    return false;
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, String recordId) {
    QueryResponseModel queryResponseModel = new QueryResponseModel();
    try {
      log.info("get refresh url ={}", refreshContext.getUrl());
      ResponseEntity<JSONObject> responseEntity = restTemplate.getForEntity(refreshContext.getUrl(),
          JSONObject.class);
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
