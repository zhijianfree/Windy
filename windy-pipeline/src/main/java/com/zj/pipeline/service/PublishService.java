package com.zj.pipeline.service;

import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.pipeline.PublishBindDto;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.IPublishBindRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
@Component
public class PublishService {

  private IPublishBindRepository publishBindRepository;
  private IBindBranchRepository bindBranchRepository;
  private IPipelineRepository pipelineRepository;
  private UniqueIdService uniqueIdService;

  public PublishService(IPublishBindRepository publishBindRepository,
      IBindBranchRepository bindBranchRepository, IPipelineRepository pipelineRepository,
      UniqueIdService uniqueIdService) {
    this.publishBindRepository = publishBindRepository;
    this.bindBranchRepository = bindBranchRepository;
    this.pipelineRepository = pipelineRepository;
    this.uniqueIdService = uniqueIdService;
  }

  public Boolean createPublish(PublishBindDto publishBindDto) {
    BindBranchDto bindBranch = bindBranchRepository.getPipelineBindBranch(
        publishBindDto.getPipelineId());
    if (Objects.isNull(bindBranch)){
      throw new ApiException(ErrorCode.PIPELINE_NOT_BIND);
    }

    PublishBindDto serviceBranch = publishBindRepository.getServiceBranch(
        publishBindDto.getServiceId(), bindBranch.getGitBranch());
    if (Objects.nonNull(serviceBranch)) {
      throw new ApiException(ErrorCode.SERVICE_BRANCH_PUBLISH_EXIST);
    }

    //设置关联服务的发布流水线
    PipelineDto publishPipeline = pipelineRepository.getPublishPipeline(
        publishBindDto.getServiceId());
    publishBindDto.setPublishLine(publishPipeline.getPipelineId());

    publishBindDto.setBranch(bindBranch.getGitBranch());
    publishBindDto.setPublishId(uniqueIdService.getUniqueId());
    return publishBindRepository.createPublish(publishBindDto);
  }

  public Boolean updatePublish(PublishBindDto publishBind) {
    return publishBindRepository.createPublish(publishBind);
  }

  public List<PublishBindDto> getPublishes(String serviceId) {
    return publishBindRepository.getServicePublishes(serviceId);
  }

  public boolean deletePublish(String publishId) {
    return publishBindRepository.deletePublish(publishId);
  }
}
