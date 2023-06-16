package com.zj.master.dispatch.pipeline.intercept;

import com.zj.common.enums.ExecuteType;
import com.zj.domain.entity.dto.log.SubDispatchLogDto;
import com.zj.domain.repository.log.ISubDispatchLogRepository;
import com.zj.master.entity.vo.DeployContext;
import com.zj.master.entity.vo.TaskNode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/6/15
 */
@Component
public class DeployNodeInterceptor implements INodeExecuteInterceptor{

  private final ISubDispatchLogRepository subDispatchLogRepository;

  public DeployNodeInterceptor(ISubDispatchLogRepository subDispatchLogRepository) {
    this.subDispatchLogRepository = subDispatchLogRepository;
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
        .filter(subLog -> Objects.equals(subLog.getExecuteType(), taskNode.getExecuteType()))
        .findFirst();
    if (optional.isPresent()) {
      DeployContext requestContext = (DeployContext) taskNode.getRequestContext();
      requestContext.setExecuteIp(optional.get().getClientIp());
      taskNode.setRequestContext(requestContext);
    }
  }
}
