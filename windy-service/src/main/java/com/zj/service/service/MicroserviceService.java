package com.zj.service.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.auth.IAuthService;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.git.IRepositoryBranch;
import com.zj.common.model.K8SContainerParams;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.auth.UserDto;
import com.zj.domain.entity.dto.feature.TestCaseDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.entity.po.service.ServiceMember;
import com.zj.domain.entity.vo.GitAccessVo;
import com.zj.domain.repository.feature.ITestCaseRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.service.entity.ServiceDto;
import com.zj.service.entity.ServiceMemberDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    private final List<IRepositoryBranch> repositoryBranches;
    private final ISystemConfigRepository systemConfig;

    public MicroserviceService(IMicroServiceRepository microServiceRepository,
                               UniqueIdService uniqueIdService, IAuthService authService,
                               List<IRepositoryBranch> gitRepositories,
                               ISystemConfigRepository systemConfig, IPipelineRepository pipelineRepository,
                               ITestCaseRepository testCaseRepository) {
        this.microServiceRepository = microServiceRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
        this.pipelineRepository = pipelineRepository;
        this.testCaseRepository = testCaseRepository;
        this.repositoryBranches = gitRepositories;
        this.systemConfig = systemConfig;
    }

    private IRepositoryBranch getRepositoryBranch() {
        GitAccessVo gitAccess = systemConfig.getGitAccess();
        return repositoryBranches.stream()
                .filter(repository -> Objects.equals(repository.gitType(), gitAccess.getGitType()))
                .findAny().orElse(null);
    }

    public PageSize<ServiceDto> getServices(Integer pageNo, Integer size, String name) {
        String currentUserId = authService.getCurrentUserId();
        List<ServiceMember> serviceMembers = microServiceRepository.getServiceMembersByUser(currentUserId);
        if (CollectionUtils.isEmpty(serviceMembers)){
            return new PageSize<>();
        }

        List<String> serviceIds = serviceMembers.stream().map(ServiceMember::getServiceId).collect(Collectors.toList());
        IPage<MicroserviceDto> page = microServiceRepository.getServices(pageNo, size, name, serviceIds);
        PageSize<ServiceDto> pageSize = new PageSize<>();
        if (CollectionUtils.isEmpty(page.getRecords())) {
            pageSize.setTotal(0);
            return pageSize;
        }

        List<ServiceDto> microservices = page.getRecords().stream()
                .map(microservice -> {
                    ServiceDto serviceDto = OrikaUtil.convert(microservice, ServiceDto.class);
                    serviceDto.setContainerParams(JSON.parseObject(microservice.getServiceConfig(),
                            K8SContainerParams.class));
                    return serviceDto;
                })
                .collect(Collectors.toList());

        pageSize.setData(microservices);
        pageSize.setTotal(page.getTotal());
        return pageSize;
    }

    public String createService(ServiceDto serviceDto) {
        IRepositoryBranch repositoryBranch = getRepositoryBranch();
        if (Objects.isNull(repositoryBranch)) {
            throw new ApiException(ErrorCode.NOT_FIND_REPO_CONFIG);
        }

        repositoryBranch.checkRepository(serviceDto.getServiceName());

        MicroserviceDto microserviceDto = OrikaUtil.convert(serviceDto, MicroserviceDto.class);
        microserviceDto.setServiceConfig(JSON.toJSONString(serviceDto.getContainerParams()));
        microserviceDto.setServiceId(uniqueIdService.getUniqueId());
        return microServiceRepository.createService(authService.getCurrentUserId(), microserviceDto);
    }

    public String updateService(ServiceDto update) {
        MicroserviceDto microserviceDto = OrikaUtil.convert(update, MicroserviceDto.class);
        Optional.ofNullable(update.getContainerParams()).ifPresent(params ->
                microserviceDto.setServiceConfig(JSON.toJSONString(params)));
        return microServiceRepository.updateService(microserviceDto);
    }

    public Boolean deleteService(String serviceId) {
        List<PipelineDto> servicePipelines = pipelineRepository.getServicePipelines(serviceId);
        if (CollectionUtils.isNotEmpty(servicePipelines)) {
            throw new ApiException(ErrorCode.SERVICE_EXIST_PIPELINE);
        }

        List<TestCaseDto> serviceCases = testCaseRepository.getServiceCases(serviceId);
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

    public List<UserDto> queryServiceMembers(String serviceId) {
        return microServiceRepository.getServiceMembers(serviceId);
    }

    public Boolean addServiceMember(ServiceMemberDto member) {
        return microServiceRepository.addServiceMember(member.getServiceId(), member.getUserId());
    }

    public Boolean deleteServiceMember(String serviceId, String userId) {
        return microServiceRepository.deleteServiceMember(serviceId, userId);
    }
}
