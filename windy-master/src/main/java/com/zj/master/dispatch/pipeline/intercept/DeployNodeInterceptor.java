package com.zj.master.dispatch.pipeline.intercept;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.DeployType;
import com.zj.common.enums.ExecuteType;
import com.zj.common.model.DeployParams;
import com.zj.common.model.K8SAccessParams;
import com.zj.common.model.ServiceConfig;
import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.domain.entity.dto.log.SubDispatchLogDto;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.dto.service.DeployEnvironmentDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
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
        List<SubDispatchLogDto> subLogs = subDispatchLogRepository.getSubLogByLogId(
                taskNode.getLogId());
        Optional<SubDispatchLogDto> optional = subLogs.stream()
                .filter(subLog -> Objects.equals(subLog.getExecuteType(), ExecuteType.BUILD.name()))
                .findFirst();
        DeployContext deployContext = (DeployContext) taskNode.getRequestContext();
        if (optional.isPresent()) {
            SubDispatchLogDto subDispatchLogDto = optional.get();
            if (!Objects.equals(deployContext.getDeployType(), DeployType.SSH.getType())) {
                findDockerImageName(subDispatchLogDto, deployContext);
            }

            deployContext.setSingleClientIp(subDispatchLogDto.getClientIp());
            deployContext.setRequestSingle(true);
        }

        DeployEnvironmentDto deployEnvironment = environmentRepository.getEnvironment(deployContext.getEnvId());
        DeployParams deployParams = getDeployParams(taskNode.getServiceId(), deployEnvironment.getEnvParams(),
                deployContext.getImageName());
        deployContext.setParams(deployParams);
        deployContext.setDeployType(deployEnvironment.getEnvType());
        taskNode.setRequestContext(deployContext);
    }

    public DeployParams getDeployParams(String serviceId, String k8sAccess, String imageName) {
        K8SAccessParams k8SAccessParams = JSON.parseObject(k8sAccess, K8SAccessParams.class);
        MicroserviceDto serviceDetail = microServiceRepository.queryServiceDetail(serviceId);
        ServiceConfig serviceConfig = JSON.parseObject(serviceDetail.getServiceConfig(),
                ServiceConfig.class);
        serviceConfig.setImageName(imageName);
        DeployParams deployParams = new DeployParams();
        deployParams.setK8SAccessParams(k8SAccessParams);
        deployParams.setServiceConfig(serviceConfig);
        return deployParams;
    }

    private void findDockerImageName(SubDispatchLogDto subDispatchLogDto, DeployContext deployContext) {
        DispatchLogDto dispatchLog = dispatchLogRepository.getDispatchLog(
                subDispatchLogDto.getLogId());
        String nodeId = subDispatchLogDto.getExecuteId();
        String pipelineHistoryId = dispatchLog.getSourceRecordId();
        NodeRecordDto nodeRecord = nodeRecordRepository.getRecordByNodeAndHistory(pipelineHistoryId,
                nodeId);
        Map<String, Object> context = JSON.parseObject(nodeRecord.getPipelineContext());
        if (Objects.nonNull(context)) {
            deployContext.setImageName(String.valueOf(context.get(IMAGE_NAME)));
        }
    }
}
