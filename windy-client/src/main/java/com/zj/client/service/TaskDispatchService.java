package com.zj.client.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.enuns.DispatchType;
import com.zj.client.feature.executor.feature.IFeatureExecutor;
import com.zj.client.feature.executor.vo.FeatureParam;
import com.zj.client.pipeline.executer.ExecuteProxy;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.common.utils.OrikaUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;

/**
 * @author falcon
 * @since 2023/5/15
 */
@Service
public class TaskDispatchService {

  private IFeatureExecutor featureExecutor;
  private ExecuteProxy executeProxy;

  private final Map<String, Function<JSONObject, Boolean>> funcMap = new HashMap<>();

  public TaskDispatchService(IFeatureExecutor featureExecutor, ExecuteProxy executeProxy) {
    this.featureExecutor = featureExecutor;
    this.executeProxy = executeProxy;
    funcMap.put(DispatchType.FEATURE.name(), this::runFeature);
    funcMap.put(DispatchType.PIPELINE.name(), this::runPipeline);
  }


  public Boolean dispatch(JSONObject params) {
    String dispatchType = params.getString("dispatchType");
    Function<JSONObject, Boolean> func = funcMap.get(dispatchType);
    return func.apply(params);
  }


  private boolean runFeature(JSONObject baseParam) {
    FeatureParam featureParam = JSON.toJavaObject(baseParam, FeatureParam.class);
    featureExecutor.execute(featureParam);
    return true;
  }

  private boolean runPipeline(JSONObject baseParam) {
    TaskNode taskNode = JSON.toJavaObject(baseParam, TaskNode.class);
    executeProxy.runNode(taskNode);
    return true;
  }

}
