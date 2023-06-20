package com.zj.client.pipeline.executer.Invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.deploy.DeployFactory;
import com.zj.client.deploy.IDeployMode;
import com.zj.client.deploy.jar.JarDeployContext;
import com.zj.client.entity.dto.ResponseModel;
import com.zj.client.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.client.pipeline.executer.vo.DeployRequest;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.RequestContext;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.client.utils.Utils;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.ProcessStatus;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class DeployInvoker implements IRemoteInvoker {

  public static final String DEPLOY = "deploy";
  private final DeployFactory deployFactory;
  private final GlobalEnvConfig globalEnvConfig;

  public DeployInvoker(DeployFactory deployFactory,
      GlobalEnvConfig globalEnvConfig) {
    this.deployFactory = deployFactory;
    this.globalEnvConfig = globalEnvConfig;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.DEPLOY;
  }

  @Override
  public boolean triggerRun(RequestContext requestContext, TaskNode taskNode) throws IOException {
    DeployRequest deployRequest = JSON.parseObject(JSON.toJSONString(requestContext.getData()),
        DeployRequest.class);
    IDeployMode deployMode = deployFactory.getDeployMode(deployRequest.getDeployType());

    String gitWorkspace = globalEnvConfig.getGitWorkspace();
    String filePath =
        gitWorkspace + File.separator + Utils.getServiceFromUrl(deployRequest.getGitUrl())
            + File.separator + DEPLOY;
    JarDeployContext jarContext = JarDeployContext.builder().sshUser(globalEnvConfig.getSShUser())
        .sshPassword(globalEnvConfig.getSSHPassword())
        .remotePath(deployRequest.getRemotePath())
        .sshIp(deployRequest.getSshIp())
        .sshPort(deployRequest.getSshPort())
        .localPath(filePath)
        .build();
    jarContext.setRecordId(taskNode.getRecordId());
    deployMode.deploy(jarContext);
    return true;
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    ProcessStatus deployStatus = deployFactory.getDeployStatus(taskNode.getRecordId());
    ResponseModel responseModel = new ResponseModel();
    responseModel.setStatus(deployStatus.getType());
    responseModel.setMessage("代码部署");
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("status", deployStatus.getType());
    responseModel.setData(jsonObject);
    return JSON.toJSONString(responseModel);
  }
}
