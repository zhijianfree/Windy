package com.zj.client.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.enuns.DispatchType;
import com.zj.client.handler.feature.executor.IFeatureExecutor;
import com.zj.client.handler.feature.executor.vo.FeatureParam;
import com.zj.client.handler.pipeline.executer.ExecuteProxy;
import com.zj.client.handler.pipeline.executer.notify.NodeStatusQueryLooper;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.common.model.StopDispatch;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Service
public class TaskDispatchService {

  public static final String DISPATCH_TYPE_KEY = "dispatchType";
  private IFeatureExecutor featureExecutor;
  private ExecuteProxy executeProxy;
  private NodeStatusQueryLooper nodeStatusQueryLooper;

  private final Map<String, Function<JSONObject, Boolean>> funcMap = new HashMap<>();

  public TaskDispatchService(IFeatureExecutor featureExecutor, ExecuteProxy executeProxy,
      NodeStatusQueryLooper nodeStatusQueryLooper) {
    this.featureExecutor = featureExecutor;
    this.executeProxy = executeProxy;
    this.nodeStatusQueryLooper = nodeStatusQueryLooper;
    funcMap.put(DispatchType.FEATURE.name(), this::runFeature);
    funcMap.put(DispatchType.PIPELINE.name(), this::runPipeline);
  }


  public Boolean dispatch(JSONObject params) {
    String dispatchType = params.getString(DISPATCH_TYPE_KEY);
    Function<JSONObject, Boolean> func = funcMap.get(dispatchType);
    return func.apply(params);
  }


  private boolean runFeature(JSONObject params) {
    FeatureParam featureParam = JSON.parseObject(JSON.toJSONString(params), FeatureParam.class);
    featureExecutor.execute(featureParam);
    return true;
  }

  private boolean runPipeline(JSONObject params) {
    TaskNode taskNode = JSON.toJavaObject(params, TaskNode.class);
    executeProxy.runNode(taskNode);
    return true;
  }

  public Boolean stopDispatch(StopDispatch stopDispatch) {
    nodeStatusQueryLooper.stopPipeline(stopDispatch.getTargetId());
    return true;
  }
}
