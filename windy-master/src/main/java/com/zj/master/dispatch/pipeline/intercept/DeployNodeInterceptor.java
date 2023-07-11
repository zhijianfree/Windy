package com.zj.master.dispatch.pipeline.intercept;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ExecuteType;
import com.zj.domain.entity.dto.log.SubDispatchLogDto;
import com.zj.domain.entity.dto.service.DeployEnvironmentDto;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.domain.repository.service.IEnvironmentRepository;
import com.zj.master.entity.vo.DeployContext;
import com.zj.master.entity.vo.TaskNode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Component
public class DeployNodeInterceptor implements INodeExecuteInterceptor{

  private final ISubDispatchLogRepository subDispatchLogRepository;
  private final IEnvironmentRepository environmentRepository;

  public DeployNodeInterceptor(ISubDispatchLogRepository subDispatchLogRepository,
      IEnvironmentRepository environmentRepository) {
    this.subDispatchLogRepository = subDispatchLogRepository;
    this.environmentRepository = environmentRepository;
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
      DeployContext requestContext = (DeployContext) taskNode.getRequestContext();
      requestContext.setSingleClientIp(optional.get().getClientIp());
      requestContext.setRequestSingle(true);
      taskNode.setRequestContext(requestContext);

      DeployEnvironmentDto deployEnvironment = environmentRepository.getEnvironment(
          requestContext.getEnvId());
      requestContext.setParams(JSON.parse(deployEnvironment.getEnvParams()));
      requestContext.setDeployType(deployEnvironment.getEnvType());
    }


  }
}
