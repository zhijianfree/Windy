package com.zj.client.pipeline.executer.Invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.deploy.DeployFactory;
import com.zj.client.deploy.IDeployMode;
import com.zj.client.deploy.jar.JarDeployContext;
import com.zj.client.entity.dto.ResponseModel;
import com.zj.client.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.client.pipeline.executer.vo.DeployRequest;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.RequestContext;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.ProcessStatus;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class DeployInvoker implements IRemoteInvoker {

  public static final String DEPLOY_SSH_USER = "deploy.ssh.user";
  public static final String DEPLOY_SSH_PWD = "deploy.ssh.pwd";
  public static final String DEFAULT_USER = "windy";
  public static final String DEFAULT_PWD = "windy!123";
  private final DeployFactory deployFactory;

  private final Environment environment;

  public DeployInvoker(DeployFactory deployFactory, Environment environment) {
    this.deployFactory = deployFactory;
    this.environment = environment;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.DEPLOY;
  }

  @Override
  public boolean triggerRun(RequestContext requestContext, String recordId) throws IOException {
    DeployRequest deployRequest = JSON.parseObject(JSON.toJSONString(requestContext.getData()),
        DeployRequest.class);
    IDeployMode deployMode = deployFactory.getDeployMode(deployRequest.getDeployType());

    String sshUser = environment.getProperty(DEPLOY_SSH_USER, DEFAULT_USER);
    String pwd = environment.getProperty(DEPLOY_SSH_PWD, DEFAULT_PWD);
    String filePath = "/Users/guyuelan/IdeaProjects/Windy/windy/Windy/target/HelloWorld-1.0-SNAPSHOT.jar";
    JarDeployContext jarContext = JarDeployContext.builder().sshUser(sshUser)
        .sshPassword(pwd)
        .remotePath(deployRequest.getRemotePath())
        .sshIp(deployRequest.getSshIp())
        .sshPort(deployRequest.getSshPort())
        .localPath(filePath)
        .build();
    jarContext.setRecordId(recordId);
    deployMode.deploy(jarContext);
    return true;
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, String recordId) {
    ProcessStatus deployStatus = deployFactory.getDeployStatus(recordId);
    ResponseModel responseModel = new ResponseModel();
    responseModel.setStatus(deployStatus.getType());
    responseModel.setMessage("代码部署");
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("status", deployStatus.getType());
    responseModel.setData(jsonObject);
    return JSON.toJSONString(responseModel);
  }
}
