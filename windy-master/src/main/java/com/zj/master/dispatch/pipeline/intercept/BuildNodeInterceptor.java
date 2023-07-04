package com.zj.master.dispatch.pipeline.intercept;

import com.zj.common.enums.ExecuteType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.entity.dto.pipeline.PublishBindDto;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.IPublishBindRepository;
import com.zj.master.entity.vo.BuildCodeContext;
import com.zj.master.entity.vo.TaskNode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Component
public class BuildNodeInterceptor implements INodeExecuteInterceptor {

  private final IPipelineNodeRepository pipelineNodeRepository;
  private final IPublishBindRepository publishBindRepository;
  private final IPipelineRepository pipelineRepository;
  private final IBindBranchRepository gitBindRepository;

  public BuildNodeInterceptor(IPipelineNodeRepository pipelineNodeRepository,
      IPublishBindRepository publishBindRepository, IPipelineRepository pipelineRepository,
      IBindBranchRepository gitBindRepository) {
    this.pipelineNodeRepository = pipelineNodeRepository;
    this.publishBindRepository = publishBindRepository;
    this.pipelineRepository = pipelineRepository;
    this.gitBindRepository = gitBindRepository;
  }

  @Override
  public void beforeExecute(TaskNode taskNode) {
    if (!Objects.equals(taskNode.getExecuteType(), ExecuteType.BUILD.name())) {
      return;
    }

    PipelineNodeDto pipelineNode = pipelineNodeRepository.getPipelineNode(taskNode.getNodeId());
    PipelineDto pipeline = pipelineRepository.getPipeline(pipelineNode.getPipelineId());
    if (Objects.equals(pipeline.getPipelineType(), PipelineType.PUBLISH.getType())) {
      //如果是发布流水线，则要查询发布的流水线分支
      List<PublishBindDto> servicePublishes = publishBindRepository.getServicePublishes(
          pipeline.getServiceId());
      List<String> branches = servicePublishes.stream().map(PublishBindDto::getBranch).collect(
          Collectors.toList());
      rebuildRequestContext(taskNode, branches, true);
      return;
    }

    BindBranchDto bindBranch = gitBindRepository.getPipelineBindBranch(
        pipelineNode.getPipelineId());
    if (Objects.isNull(bindBranch)) {
      throw new ApiException(ErrorCode.NOT_FIND_BRANCH);
    }
    rebuildRequestContext(taskNode, Collections.singletonList(bindBranch.getGitBranch()), false);
  }

  private static void rebuildRequestContext(TaskNode taskNode, List<String> branches, boolean isPublish) {
    BuildCodeContext requestContext = (BuildCodeContext) taskNode.getRequestContext();
    requestContext.setBranches(branches);
    requestContext.setIsPublish(isPublish);
    taskNode.setRequestContext(requestContext);
  }
}
