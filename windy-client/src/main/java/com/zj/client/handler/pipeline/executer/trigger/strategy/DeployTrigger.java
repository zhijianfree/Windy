package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.handler.deploy.DeployContext;
import com.zj.client.handler.deploy.DeployFactory;
import com.zj.client.handler.deploy.IDeployMode;
import com.zj.client.handler.deploy.jar.JarDeployContext;
import com.zj.client.handler.deploy.k8s.K8sDeployContext;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.DeployRequest;
import com.zj.client.handler.pipeline.executer.vo.DeployRequest.SSHParams;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.handler.pipeline.executer.vo.TriggerContext;
import com.zj.client.utils.Utils;
import com.zj.common.enums.DeployType;
import com.zj.common.enums.ExecuteType;
import com.zj.common.exception.ExecuteException;
import com.zj.common.model.DeployParams;
import com.zj.common.model.K8SAccessParams;
import com.zj.common.model.ServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Jar部署处理
 *
 * @author guyuelan
 * @since 2023/5/8
 */
@Slf4j
@Component
public class DeployTrigger implements INodeTrigger {

    public static final String DEPLOY = "deploy";
    private final DeployFactory deployFactory;
    private final GlobalEnvConfig globalEnvConfig;

    private final Map<Integer, Function<DeployRequest, DeployContext>> functionMap = new HashMap<>();

    public DeployTrigger(DeployFactory deployFactory, GlobalEnvConfig globalEnvConfig) {
        this.deployFactory = deployFactory;
        this.globalEnvConfig = globalEnvConfig;
        functionMap.put(DeployType.SSH.getType(), this::buildSSHContext);
        functionMap.put(DeployType.K8S.getType(), this::buildK8SContext);
    }

    @Override
    public ExecuteType type() {
        return ExecuteType.DEPLOY;
    }

    @Override
    public void triggerRun(TriggerContext triggerContext, TaskNode taskNode) throws IOException {
        log.info("triggerContext = {}", triggerContext.getData());
        DeployRequest deployRequest = JSON.parseObject(JSON.toJSONString(triggerContext.getData()),
                DeployRequest.class);
        Function<DeployRequest, DeployContext> function = functionMap.get(deployRequest.getDeployType());
        if (Objects.isNull(function)) {
            throw new ExecuteException("can not find deploy type");
        }
        DeployContext deployContext = function.apply(deployRequest);
        deployContext.setRecordId(taskNode.getRecordId());

        IDeployMode deployMode = deployFactory.getDeployMode(deployRequest.getDeployType());
        deployMode.deploy(deployContext);
    }

    @Override
    public QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
        return deployFactory.getDeployStatus(taskNode.getRecordId());
    }

    private JarDeployContext buildSSHContext(DeployRequest deployRequest) {
        String serviceName = Utils.getServiceFromUrl(deployRequest.getGitUrl());
        String filePath =
                globalEnvConfig.getPipelineWorkspace(serviceName, deployRequest.getPipelineId()) + File.separator + DEPLOY;
        SSHParams sshParams = JSON.parseObject(JSON.toJSONString(deployRequest.getParams()), SSHParams.class);
        String serverPort = Optional.ofNullable(deployRequest.getServerPort()).orElse("");
        return JarDeployContext.builder().sshUser(sshParams.getUser()).sshPassword(sshParams.getPassword()).remotePath(sshParams.getRemotePath()).sshIp(sshParams.getSshIp()).sshPort(sshParams.getSshPort()).localPath(filePath).servicePort(serverPort).build();
    }

    private K8sDeployContext buildK8SContext(DeployRequest deployRequest) {
        String serviceName = Utils.getServiceFromUrl(deployRequest.getGitUrl());
        DeployParams deployParams = JSON.parseObject(JSON.toJSONString(deployRequest.getParams()), DeployParams.class);
        K8SAccessParams k8SAccessParams = deployParams.getK8SAccessParams();
        ServiceConfig serviceConfig = deployParams.getServiceConfig();
        return K8sDeployContext.builder().serviceConfig(serviceConfig).k8SAccessParams(k8SAccessParams).serviceName(serviceName).build();
    }
}
