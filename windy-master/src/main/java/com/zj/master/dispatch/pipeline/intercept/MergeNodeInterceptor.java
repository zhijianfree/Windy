package com.zj.master.dispatch.pipeline.intercept;

import com.zj.common.enums.ExecuteType;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.entity.dto.pipeline.PublishBindDto;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.IPublishBindRepository;
import com.zj.master.entity.vo.MergeMasterContext;
import com.zj.master.entity.vo.TaskNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 只处理代码合并的节点
 * @author guyuelan
 * @since 2023/6/29
 */
@Slf4j
@Component
public class MergeNodeInterceptor implements INodeExecuteInterceptor{
  private final IPipelineNodeRepository pipelineNodeRepository;
  private final IPublishBindRepository publishBindRepository;
  private final IPipelineRepository pipelineRepository;

  public MergeNodeInterceptor(IPipelineNodeRepository pipelineNodeRepository,
      IPublishBindRepository publishBindRepository, IPipelineRepository pipelineRepository) {
    this.pipelineNodeRepository = pipelineNodeRepository;
    this.publishBindRepository = publishBindRepository;
    this.pipelineRepository = pipelineRepository;
  }

  @Override
  public int sort() {
    return 3;
  }

  @Override
  public void beforeExecute(TaskNode taskNode) {
    if (!Objects.equals(taskNode.getExecuteType(), ExecuteType.MERGE.name())) {
      return;
    }

    PipelineNodeDto pipelineNode = pipelineNodeRepository.getPipelineNode(taskNode.getNodeId());
    PipelineDto pipeline = pipelineRepository.getPipeline(pipelineNode.getPipelineId());
    List<PublishBindDto> servicePublishes = publishBindRepository.getServicePublishes(
        pipeline.getServiceId());
    List<String> branches = servicePublishes.stream().map(PublishBindDto::getBranch).collect(
        Collectors.toList());
    MergeMasterContext requestContext = (MergeMasterContext) taskNode.getRequestContext();
    requestContext.setBranches(branches);
    taskNode.setRequestContext(requestContext);
  }
}
