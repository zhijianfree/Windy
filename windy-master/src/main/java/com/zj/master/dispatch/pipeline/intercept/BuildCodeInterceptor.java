package com.zj.master.dispatch.pipeline.intercept;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.ExecuteType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ServiceConfig;
import com.zj.domain.entity.dto.pipeline.BindBranchDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDto;
import com.zj.domain.entity.dto.pipeline.PublishBindDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.entity.vo.ImageRepositoryVo;
import com.zj.domain.repository.pipeline.IBindBranchRepository;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.IPublishBindRepository;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.master.entity.vo.BuildCodeContext;
import com.zj.master.entity.vo.PipelineConfig;
import com.zj.master.entity.vo.TaskNode;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Component
public class BuildCodeInterceptor implements INodeExecuteInterceptor {

    private final IPipelineNodeRepository pipelineNodeRepository;
    private final IPublishBindRepository publishBindRepository;
    private final IPipelineRepository pipelineRepository;
    private final IBindBranchRepository gitBindRepository;
    private final ISystemConfigRepository configRepository;
    private final IMicroServiceRepository microServiceRepository;

    public BuildCodeInterceptor(IPipelineNodeRepository pipelineNodeRepository,
                                IPublishBindRepository publishBindRepository, IPipelineRepository pipelineRepository,
                                IBindBranchRepository gitBindRepository, ISystemConfigRepository configRepository,
                                IMicroServiceRepository microServiceRepository) {
        this.pipelineNodeRepository = pipelineNodeRepository;
        this.publishBindRepository = publishBindRepository;
        this.pipelineRepository = pipelineRepository;
        this.gitBindRepository = gitBindRepository;
        this.configRepository = configRepository;
        this.microServiceRepository = microServiceRepository;
    }

    @Override
    public int sort() {
        return 2;
    }

    @Override
    public void beforeExecute(TaskNode taskNode) {
        if (!Objects.equals(taskNode.getExecuteType(), ExecuteType.BUILD.name())) {
            return;
        }

        ImageRepositoryVo repository = configRepository.getRepository();
        PipelineNodeDto pipelineNode = pipelineNodeRepository.getPipelineNode(taskNode.getNodeId());
        PipelineDto pipeline = pipelineRepository.getPipeline(pipelineNode.getPipelineId());
        MicroserviceDto serviceDetail = microServiceRepository.queryServiceDetail(pipeline.getServiceId());
        String config = serviceDetail.getServiceConfig();
        ServiceConfig serviceConfig = JSON.parseObject(config, ServiceConfig.class);
        if (Objects.equals(pipeline.getPipelineType(), PipelineType.PUBLISH.getType())) {
            //如果是发布流水线，则要查询发布的流水线分支
            List<PublishBindDto> servicePublishes = publishBindRepository.getServicePublishes(pipeline.getServiceId());
            List<String> branches =
                    servicePublishes.stream().map(PublishBindDto::getBranch).collect(Collectors.toList());
            PipelineConfig pipelineConfig = JSON.parseObject(pipeline.getPipelineConfig(), PipelineConfig.class);
            String version = null;
            if (Objects.nonNull(pipelineConfig) && MapUtils.isNotEmpty(pipelineConfig.getParamList())) {
                version =
                        Optional.ofNullable(pipelineConfig.getParamList().get("version")).map(String::valueOf).orElse(null);
            }
            rebuildRequestContext(taskNode, branches, repository, true, serviceConfig, version);
            return;
        }

        BindBranchDto bindBranch = gitBindRepository.getPipelineBindBranch(pipelineNode.getPipelineId());
        if (Objects.isNull(bindBranch)) {
            throw new ApiException(ErrorCode.NOT_FIND_BRANCH);
        }
        rebuildRequestContext(taskNode, Collections.singletonList(bindBranch.getGitBranch()), repository, false,
                serviceConfig, null);
    }

    private void rebuildRequestContext(TaskNode taskNode, List<String> branches, ImageRepositoryVo repository,
                                       boolean isPublish, ServiceConfig serviceConfig, String version) {
        BuildCodeContext buildCodeContext = (BuildCodeContext) taskNode.getRequestContext();
        buildCodeContext.setVersion(version);
        buildCodeContext.setBranches(branches);
        buildCodeContext.setIsPublish(isPublish);
        buildCodeContext.setServiceName(serviceConfig.getAppName());
        if (Objects.nonNull(repository)) {
            buildCodeContext.setUser(repository.getUserName());
            buildCodeContext.setPassword(repository.getPassword());
            buildCodeContext.setRepository(repository.getRepositoryUrl());
        }
        taskNode.setRequestContext(buildCodeContext);
    }
}
