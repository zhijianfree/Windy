package com.zj.service.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.adapter.git.IGitRepositoryHandler;
import com.zj.common.entity.pipeline.ServiceConfig;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.feature.TestCaseBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.service.MicroserviceDto;
import com.zj.domain.entity.po.service.ResourceMember;
import com.zj.domain.repository.demand.IMemberRepository;
import com.zj.domain.repository.feature.ITestCaseRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.service.entity.ServiceDto;
import com.zj.domain.entity.bo.service.ResourceMemberDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MicroserviceService {

    private final IMicroServiceRepository microServiceRepository;
    private final UniqueIdService uniqueIdService;
    private final IPipelineRepository pipelineRepository;
    private final IAuthService authService;
    private final ITestCaseRepository testCaseRepository;
    private final List<IGitRepositoryHandler> repositoryBranches;
    private final ISystemConfigRepository systemConfig;
    private final IMemberRepository memberRepository;

    public MicroserviceService(IMicroServiceRepository microServiceRepository,
                               UniqueIdService uniqueIdService, IAuthService authService,
                               List<IGitRepositoryHandler> gitRepositories,
                               ISystemConfigRepository systemConfig, IPipelineRepository pipelineRepository,
                               ITestCaseRepository testCaseRepository, IMemberRepository memberRepository) {
        this.microServiceRepository = microServiceRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
        this.pipelineRepository = pipelineRepository;
        this.testCaseRepository = testCaseRepository;
        this.repositoryBranches = gitRepositories;
        this.systemConfig = systemConfig;
        this.memberRepository = memberRepository;
    }

    private IGitRepositoryHandler getRepositoryBranch(String type) {
        return repositoryBranches.stream()
                .filter(repository -> Objects.equals(repository.gitType(), type))
                .findAny().orElse(null);
    }

    public PageSize<ServiceDto> getServices(Integer pageNo, Integer size, String name) {
        String currentUserId = authService.getCurrentUserId();
        List<ResourceMember> resourceMembers = memberRepository.getResourceMembersByUser(currentUserId);
        if (CollectionUtils.isEmpty(resourceMembers)){
            return new PageSize<>();
        }

        List<String> serviceIds = resourceMembers.stream().map(ResourceMember::getResourceId).collect(Collectors.toList());
        IPage<MicroserviceDto> page = microServiceRepository.getServices(pageNo, size, name, serviceIds);
        PageSize<ServiceDto> pageSize = new PageSize<>();
        if (CollectionUtils.isEmpty(page.getRecords())) {
            pageSize.setTotal(0);
            return pageSize;
        }

        List<ServiceDto> microservices = page.getRecords().stream()
                .map(microservice -> {
                    ServiceDto serviceDto = OrikaUtil.convert(microservice, ServiceDto.class);
                    serviceDto.setServiceConfig(JSON.parseObject(microservice.getServiceConfig(),
                            ServiceConfig.class));
                    return serviceDto;
                })
                .collect(Collectors.toList());

        pageSize.setData(microservices);
        pageSize.setTotal(page.getTotal());
        return pageSize;
    }

    public String createService(ServiceDto serviceDto) {
        IGitRepositoryHandler repositoryBranch = getRepositoryBranch(chooseGitType(serviceDto));
        if (Objects.isNull(repositoryBranch)) {
            log.info("can not find git repository config ={}", serviceDto.getServiceName());
            throw new ApiException(ErrorCode.NOT_FIND_REPO_CONFIG);
        }

        GitAccessInfo gitAccessInfo = Optional.ofNullable(serviceDto.getServiceConfig())
                .map(ServiceConfig::getGitAccessInfo).filter(access -> StringUtils.isNotBlank(access.getAccessToken()))
                .orElseGet(systemConfig::getGitAccess);
        gitAccessInfo.setGitUrl(serviceDto.getGitUrl());
        repositoryBranch.checkRepository(gitAccessInfo);

        MicroserviceDto microserviceDto = OrikaUtil.convert(serviceDto, MicroserviceDto.class);
        microserviceDto.setServiceConfig(JSON.toJSONString(serviceDto.getServiceConfig()));
        microserviceDto.setServiceId(uniqueIdService.getUniqueId());
        return microServiceRepository.createService(authService.getCurrentUserId(), microserviceDto);
    }

    private String chooseGitType(ServiceDto serviceDto) {
        if (Objects.isNull(serviceDto.getServiceConfig()) || Objects.isNull(serviceDto.getServiceConfig().getGitAccessInfo())
                || StringUtils.isBlank(serviceDto.getServiceConfig().getGitAccessInfo().getAccessToken())) {
            return systemConfig.getGitAccess().getGitType();
        }
        return serviceDto.getServiceConfig().getGitAccessInfo().getGitType();
    }

    public String updateService(ServiceDto update) {
        MicroserviceDto microserviceDto = OrikaUtil.convert(update, MicroserviceDto.class);
        Optional.ofNullable(update.getServiceConfig()).ifPresent(params ->
                microserviceDto.setServiceConfig(JSON.toJSONString(params)));
        return microServiceRepository.updateService(microserviceDto);
    }

    public Boolean deleteService(String serviceId) {
        List<PipelineBO> servicePipelines = pipelineRepository.getServicePipelines(serviceId);
        if (CollectionUtils.isNotEmpty(servicePipelines)) {
            throw new ApiException(ErrorCode.SERVICE_EXIST_PIPELINE);
        }

        List<TestCaseBO> serviceCases = testCaseRepository.getServiceCases(serviceId);
        if (CollectionUtils.isNotEmpty(serviceCases)) {
            throw new ApiException(ErrorCode.SERVICE_EXIST_FEATURE);
        }
        return microServiceRepository.deleteService(serviceId);
    }

    public MicroserviceDto queryServiceDetail(String serviceId) {
        return microServiceRepository.queryServiceDetail(serviceId);
    }

    public MicroserviceDto queryServiceByName(String serviceName) {
        return microServiceRepository.queryServiceByName(serviceName);
    }

    public List<MicroserviceDto> getServices() {
        String currentUserId = authService.getCurrentUserId();
        return microServiceRepository.getServices(currentUserId).stream()
                .sorted(Comparator.comparing(MicroserviceDto::getPriority).reversed()).collect(
                        Collectors.toList());
    }

    public List<UserBO> queryServiceMembers(String serviceId) {
        return memberRepository.queryResourceMembers(serviceId);
    }

    public Boolean addServiceMember(ResourceMemberDto member) {
        return memberRepository.addResourceMember(member);
    }

    public Boolean deleteServiceMember(String serviceId, String userId) {
        return memberRepository.deleteResourceMember(serviceId, userId);
    }
}
