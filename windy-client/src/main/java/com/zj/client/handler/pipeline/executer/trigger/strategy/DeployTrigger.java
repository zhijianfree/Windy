package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.handler.deploy.DeployFactory;
import com.zj.client.handler.deploy.IDeployMode;
import com.zj.client.handler.deploy.jar.JarDeployContext;
import com.zj.client.entity.dto.ResponseModel;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.DeployRequest;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TriggerContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.utils.Utils;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.ProcessStatus;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Jar部署处理
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class DeployTrigger implements INodeTrigger {

  public static final String DEPLOY = "deploy";
  private final DeployFactory deployFactory;
  private final GlobalEnvConfig globalEnvConfig;

  public DeployTrigger(DeployFactory deployFactory,
      GlobalEnvConfig globalEnvConfig) {
    this.deployFactory = deployFactory;
    this.globalEnvConfig = globalEnvConfig;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.DEPLOY;
  }

  @Override
  public void triggerRun(TriggerContext triggerContext, TaskNode taskNode) throws IOException {
    DeployRequest deployRequest = JSON.parseObject(JSON.toJSONString(triggerContext.getData()),
        DeployRequest.class);
    String serviceName = Utils.getServiceFromUrl(deployRequest.getGitUrl());
    String filePath =
        globalEnvConfig.getPipelineWorkspace(serviceName, deployRequest.getPipelineId())
            + File.separator + DEPLOY;
    JarDeployContext jarContext = JarDeployContext.builder().sshUser(globalEnvConfig.getSShUser())
        .sshPassword(globalEnvConfig.getSSHPassword())
        .remotePath(deployRequest.getRemotePath())
        .sshIp(deployRequest.getSshIp())
        .sshPort(deployRequest.getSshPort())
        .localPath(filePath)
        .servicePort(deployRequest.getServerPort())
        .build();
    jarContext.setRecordId(taskNode.getRecordId());

    IDeployMode deployMode = deployFactory.getDeployMode(deployRequest.getDeployType());
    deployMode.deploy(jarContext);
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    ProcessStatus deployStatus = deployFactory.getDeployStatus(taskNode.getRecordId());
    ResponseModel responseModel = new ResponseModel();
    responseModel.setStatus(deployStatus.getType());
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("status", deployStatus.getType());
    responseModel.setData(jsonObject);
    return JSON.toJSONString(responseModel);
  }
}
