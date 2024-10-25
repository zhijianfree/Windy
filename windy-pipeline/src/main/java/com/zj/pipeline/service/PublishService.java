package com.zj.pipeline.service;

import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import com.zj.domain.entity.dto.pipeline.PipelineHistoryDto;
import com.zj.domain.entity.dto.pipeline.PublishBindDto;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.IPipelineHistoryRepository;
import com.zj.domain.repository.pipeline.IPublishBindRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
@Slf4j
@Component
public class PublishService {

    private final IPublishBindRepository publishBindRepository;
    private final IBindBranchRepository bindBranchRepository;
    private final IPipelineHistoryRepository pipelineHistoryRepository;
    private final UniqueIdService uniqueIdService;

    public PublishService(IPublishBindRepository publishBindRepository, IBindBranchRepository bindBranchRepository,
                          IPipelineHistoryRepository pipelineHistoryRepository, UniqueIdService uniqueIdService) {
        this.publishBindRepository = publishBindRepository;
        this.bindBranchRepository = bindBranchRepository;
        this.pipelineHistoryRepository = pipelineHistoryRepository;
        this.uniqueIdService = uniqueIdService;
    }

    public Boolean createPublish(PublishBindDto publishBindDto) {
        BindBranchDto bindBranch = bindBranchRepository.getPipelineBindBranch(
                publishBindDto.getPipelineId());
        if (Objects.isNull(bindBranch)) {
            throw new ApiException(ErrorCode.PIPELINE_NOT_BIND);
        }

        PublishBindDto serviceBranch = publishBindRepository.getServiceBranch(
                publishBindDto.getServiceId(), bindBranch.getGitBranch());
        if (Objects.nonNull(serviceBranch)) {
            throw new ApiException(ErrorCode.SERVICE_BRANCH_PUBLISH_EXIST);
        }

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
        PublishBindDto publish = publishBindRepository.getPublishById(publishId);
        String pipelineId = publish.getPipelineId();
        PipelineHistoryDto latestPipelineHistory = pipelineHistoryRepository.getLatestPipelineHistory(pipelineId);
        if (Objects.nonNull(latestPipelineHistory) && Objects.equals(latestPipelineHistory.getPipelineStatus(),
                ProcessStatus.RUNNING.getType())) {
            log.info("pipeline is running , can not delete publish={}", publishId);
            throw new ApiException(ErrorCode.PIPELINE_RUNNING_NOT_DELETE_PUBLISH);
        }
        return publishBindRepository.deletePublish(publishId);
    }
}
