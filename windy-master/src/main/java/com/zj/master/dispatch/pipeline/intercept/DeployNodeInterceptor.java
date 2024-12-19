package com.zj.master.dispatch.pipeline.intercept;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.DeployType;
import com.zj.common.enums.ExecuteType;
import com.zj.common.entity.pipeline.DeployParams;
import com.zj.common.entity.pipeline.K8SAccessParams;
import com.zj.common.entity.pipeline.SSHParams;
import com.zj.common.entity.pipeline.ServiceConfig;
import com.zj.domain.entity.bo.log.DispatchLogBO;
import com.zj.domain.entity.bo.log.SubDispatchLogBO;
import com.zj.domain.entity.bo.pipeline.NodeRecordBO;
import com.zj.domain.entity.bo.service.DeployEnvironmentBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.entity.enums.EnvType;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.domain.repository.service.IEnvironmentRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.master.entity.vo.DeployContext;
import com.zj.master.entity.vo.TaskNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 部署节点前置处理
 *
 * @author guyuelan
 * @since 2023/6/15
 */
@Component
public class DeployNodeInterceptor implements INodeExecuteInterceptor {

    public static final String IMAGE_NAME = "imageName";
    private final ISubDispatchLogRepository subDispatchLogRepository;
    private final IEnvironmentRepository environmentRepository;
    private final IDispatchLogRepository dispatchLogRepository;
    private final INodeRecordRepository nodeRecordRepository;
    private final IMicroServiceRepository microServiceRepository;

    public DeployNodeInterceptor(ISubDispatchLogRepository subDispatchLogRepository,
                                 IEnvironmentRepository environmentRepository,
                                 IDispatchLogRepository dispatchLogRepository,
                                 INodeRecordRepository nodeRecordRepository,
                                 IMicroServiceRepository microServiceRepository) {
        this.subDispatchLogRepository = subDispatchLogRepository;
        this.environmentRepository = environmentRepository;
        this.dispatchLogRepository = dispatchLogRepository;
        this.nodeRecordRepository = nodeRecordRepository;
        this.microServiceRepository = microServiceRepository;
    }

    @Override
    public int sort() {
        return 4;
    }

    @Override
    public void beforeExecute(TaskNode taskNode) {
        //部署节点运行需要知道之前构建节点在哪里， 否则在JAR部署场景就会出现找不到文件的问题
        if (!Objects.equals(taskNode.getExecuteType(), ExecuteType.DEPLOY.name())) {
            return;
        }
        List<SubDispatchLogBO> subLogs = subDispatchLogRepository.getSubLogByLogId(
                taskNode.getLogId());
        Optional<SubDispatchLogBO> optional = subLogs.stream()
                .filter(subLog -> Objects.equals(subLog.getExecuteType(), ExecuteType.BUILD.name()))
                .findFirst();
        DeployContext deployContext = (DeployContext) taskNode.getRequestContext();
        if (optional.isPresent()) {
            SubDispatchLogBO subDispatchLogBO = optional.get();
            if (!Objects.equals(deployContext.getDeployType(), DeployType.SSH.getType())) {
                findDockerImageName(subDispatchLogBO, deployContext);
            }

            deployContext.setSingleClientIp(subDispatchLogBO.getClientIp());
            deployContext.setRequestSingle(true);
        }

        DeployEnvironmentBO deployEnvironment = environmentRepository.getEnvironment(deployContext.getEnvId());
        DeployParams deployParams = getDeployParams(taskNode.getServiceId(), deployEnvironment, deployContext.getImageName());
        deployContext.setParams(deployParams);
        deployContext.setDeployType(deployEnvironment.getEnvType());
        taskNode.setRequestContext(deployContext);
    }

    public DeployParams getDeployParams(String serviceId, DeployEnvironmentBO environment, String imageName) {
        DeployParams deployParams = new DeployParams();
        if (Objects.equals(environment.getEnvType(), EnvType.K8S.getType())) {
            K8SAccessParams k8SAccessParams = JSON.parseObject(environment.getEnvParams(), K8SAccessParams.class);
            deployParams.setK8SAccessParams(k8SAccessParams);
        }

        if (Objects.equals(environment.getEnvType(), EnvType.SSH.getType())) {
            SSHParams sshParams = JSON.parseObject(environment.getEnvParams(), SSHParams.class);
            deployParams.setSshParams(sshParams);
        }

        MicroserviceBO serviceDetail = microServiceRepository.queryServiceDetail(serviceId);
        ServiceConfig serviceConfig = serviceDetail.getServiceConfig();
        serviceConfig.setImageName(imageName);
        deployParams.setServiceConfig(serviceConfig);
        return deployParams;
    }

    private void findDockerImageName(SubDispatchLogBO subDispatchLogBO, DeployContext deployContext) {
        DispatchLogBO dispatchLog = dispatchLogRepository.getDispatchLog(
                subDispatchLogBO.getLogId());
        String nodeId = subDispatchLogBO.getExecuteId();
        String pipelineHistoryId = dispatchLog.getSourceRecordId();
        NodeRecordBO nodeRecord = nodeRecordRepository.getRecordByNodeAndHistory(pipelineHistoryId,
                nodeId);
        Map<String, Object> context = nodeRecord.getPipelineContext();
        if (Objects.nonNull(context)) {
            deployContext.setImageName(String.valueOf(context.get(IMAGE_NAME)));
        }
    }
}
