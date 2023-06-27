package com.zj.master.dispatch.pipeline.intercept;

import com.zj.common.enums.ExecuteType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import com.zj.master.entity.vo.BuildCodeContext;
import com.zj.master.entity.vo.TaskNode;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Component
public class BuildNodeInterceptor implements INodeExecuteInterceptor {

  private final IPipelineNodeRepository pipelineNodeRepository;
  private final IBindBranchRepository gitBindRepository;

  public BuildNodeInterceptor(IPipelineNodeRepository pipelineNodeRepository,
      IBindBranchRepository gitBindRepository) {
    this.pipelineNodeRepository = pipelineNodeRepository;
    this.gitBindRepository = gitBindRepository;
  }

  @Override
  public void beforeExecute(TaskNode taskNode) {
    if (!Objects.equals(taskNode.getExecuteType(), ExecuteType.BUILD.name())) {
      return;
    }

    PipelineNodeDto pipelineNode = pipelineNodeRepository.getPipelineNode(taskNode.getNodeId());
    BindBranchDto bindBranch = gitBindRepository.getPipelineBindBranch(
        pipelineNode.getPipelineId());
    if (Objects.isNull(bindBranch)) {
      throw new ApiException(ErrorCode.NOT_FIND_BRANCH);
    }
    BuildCodeContext requestContext = (BuildCodeContext) taskNode.getRequestContext();
    requestContext.setBranch(bindBranch.getGitBranch());
    taskNode.setRequestContext(requestContext);
  }
}
