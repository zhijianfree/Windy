package com.zj.client.service;

import com.zj.client.entity.dto.BaseParam;
import com.zj.client.entity.enuns.DispatchType;
import com.zj.client.feature.executor.feature.IFeatureExecutor;
import com.zj.client.feature.executor.vo.FeatureParam;
import com.zj.client.pipeline.executer.ExecuteProxy;
import com.zj.client.pipeline.executer.vo.TaskNode;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author falcon
 * @since 2023/5/15
 */
@Service
public class TaskDispatchService {

  private IFeatureExecutor featureExecutor;
  private ExecuteProxy executeProxy;

  private final Map<String, Function<BaseParam, Boolean>> funcMap = new HashMap<>();

  public TaskDispatchService(IFeatureExecutor featureExecutor, ExecuteProxy executeProxy) {
    this.featureExecutor = featureExecutor;
    this.executeProxy = executeProxy;
    funcMap.put(DispatchType.FEATURE.name(), this::runFeature);
    funcMap.put(DispatchType.PIPELINE.name(), this::runPipeline);
  }


  public Boolean dispatch(BaseParam baseParam) {
    String dispatchType = baseParam.getDispatchType();
    Function<BaseParam, Boolean> func = funcMap.get(dispatchType);
    return func.apply(baseParam);
  }


  private boolean runFeature(BaseParam baseParam) {
    FeatureParam featureParam = (FeatureParam) baseParam;
    featureExecutor.execute(featureParam);
    return true;
  }

  private boolean runPipeline(BaseParam baseParam) {
    TaskNode taskNode = (TaskNode) baseParam;
    executeProxy.runNode(taskNode);
    return true;
  }

}
