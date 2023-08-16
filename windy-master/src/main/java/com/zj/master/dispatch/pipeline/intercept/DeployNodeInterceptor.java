package com.zj.master.dispatch.pipeline.intercept;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.DeployType;
import com.zj.common.enums.ExecuteType;
import com.zj.domain.entity.dto.log.DispatchLogDto;
import com.zj.domain.entity.dto.log.SubDispatchLogDto;
import com.zj.domain.entity.dto.pipeline.NodeRecordDto;
import com.zj.domain.entity.dto.service.DeployEnvironmentDto;
import com.zj.domain.repository.log.IDispatchLogRepository;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.domain.repository.pipeline.INodeRecordRepository;
import com.zj.domain.repository.service.IEnvironmentRepository;
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

  public DeployNodeInterceptor(ISubDispatchLogRepository subDispatchLogRepository,
      IEnvironmentRepository environmentRepository, IDispatchLogRepository dispatchLogRepository,
      INodeRecordRepository nodeRecordRepository) {
    this.subDispatchLogRepository = subDispatchLogRepository;
    this.environmentRepository = environmentRepository;
    this.dispatchLogRepository = dispatchLogRepository;
    this.nodeRecordRepository = nodeRecordRepository;
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
    if (optional.isPresent()) {
      SubDispatchLogDto subDispatchLogDto = optional.get();
      DeployContext requestContext = (DeployContext) taskNode.getRequestContext();
      if (!Objects.equals(requestContext.getDeployType(), DeployType.SSH.getType())) {
        findDockerImageName(subDispatchLogDto, requestContext);
      }

      requestContext.setSingleClientIp(subDispatchLogDto.getClientIp());
      requestContext.setRequestSingle(true);
      taskNode.setRequestContext(requestContext);

      DeployEnvironmentDto deployEnvironment = environmentRepository.getEnvironment(
          requestContext.getEnvId());
      requestContext.setParams(JSON.parse(deployEnvironment.getEnvParams()));
      requestContext.setDeployType(deployEnvironment.getEnvType());
    }

  }

  private void findDockerImageName(SubDispatchLogDto subDispatchLogDto, DeployContext requestContext) {
    DispatchLogDto dispatchLog = dispatchLogRepository.getDispatchLog(
        subDispatchLogDto.getLogId());
    String nodeId = subDispatchLogDto.getExecuteId();
    String pipelineHistoryId = dispatchLog.getSourceRecordId();
    NodeRecordDto nodeRecord = nodeRecordRepository.getRecordByNodeAndHistory(pipelineHistoryId,
        nodeId);
    Map<String, Object> context = JSON.parseObject(nodeRecord.getPipelineContext());
    if (Objects.nonNull(context)) {
      requestContext.setImageName(String.valueOf(context.get(IMAGE_NAME)));
    }
  }
}
