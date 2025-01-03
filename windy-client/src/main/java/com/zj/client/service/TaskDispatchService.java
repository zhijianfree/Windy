package com.zj.client.service;

import com.zj.client.entity.dto.GenerateDto;
import com.zj.client.handler.feature.executor.IFeatureExecutor;
import com.zj.client.handler.feature.executor.vo.FeatureParam;
import com.zj.client.handler.generate.MavenGenerator;
import com.zj.client.handler.pipeline.executer.ExecuteProxy;
import com.zj.client.handler.pipeline.executer.notify.NodeStatusQueryLooper;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.common.entity.dto.StopDispatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Slf4j
@Service
public class TaskDispatchService {
  private final IFeatureExecutor featureExecutor;
  private final ExecuteProxy executeProxy;
  private final NodeStatusQueryLooper nodeStatusQueryLooper;
  private final MavenGenerator mavenGenerator;

  public TaskDispatchService(IFeatureExecutor featureExecutor, ExecuteProxy executeProxy,
      NodeStatusQueryLooper nodeStatusQueryLooper, MavenGenerator mavenGenerator) {
    this.featureExecutor = featureExecutor;
    this.executeProxy = executeProxy;
    this.nodeStatusQueryLooper = nodeStatusQueryLooper;
    this.mavenGenerator = mavenGenerator;

  }

  public boolean runFeature(FeatureParam featureParam) {
    featureExecutor.execute(featureParam);
    return true;
  }

  public boolean runPipeline(TaskNode taskNode) {
    executeProxy.runNode(taskNode);
    return true;
  }

  public boolean runGenerate(GenerateDto generate) {
    mavenGenerator.startGenerate(generate);
    return true;
  }

  public Boolean stopDispatch(StopDispatch stopDispatch) {
    log.info("receive stop message={}", stopDispatch.getTargetId());
    nodeStatusQueryLooper.stopPipeline(stopDispatch.getTargetId());
    return true;
  }
}
