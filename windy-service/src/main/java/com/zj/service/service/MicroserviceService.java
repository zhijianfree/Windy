package com.zj.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.adapter.git.IGitRepositoryHandler;
import com.zj.common.adapter.invoker.IClientInvoker;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.WindyConstants;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.entity.pipeline.ServiceConfig;
import com.zj.common.entity.service.ToolLoadResult;
import com.zj.common.entity.service.ToolVersionDto;
import com.zj.common.enums.ApiType;
import com.zj.common.enums.InvokerType;
import com.zj.common.enums.TemplateType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.CommonException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.feature.ExecutePointBO;
import com.zj.domain.entity.bo.feature.ExecuteTemplateBO;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;
import com.zj.domain.entity.bo.feature.TestCaseBO;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.service.BuildToolBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.entity.bo.service.ResourceMemberBO;
import com.zj.domain.entity.bo.service.ServiceApiBO;
import com.zj.domain.entity.enums.FeatureType;
import com.zj.domain.entity.enums.MemberType;
import com.zj.domain.repository.demand.IMemberRepository;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.feature.ITestCaseRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IBuildToolRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.domain.repository.service.IServiceApiRepository;
import com.zj.service.entity.ServiceDto;
import com.zj.service.entity.ServiceMemberDto;
import com.zj.service.entity.ServiceStaticsDto;
import com.zj.service.entity.SystemBuildDto;
import com.zj.service.entity.SystemVersion;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MicroserviceService {

    @Value("${windy.console.version}")
    private String consoleVersion;
    private final IMicroServiceRepository microServiceRepository;
    private final UniqueIdService uniqueIdService;
    private final IPipelineRepository pipelineRepository;
    private final IAuthService authService;
    private final ITestCaseRepository testCaseRepository;
    private final List<IGitRepositoryHandler> repositoryBranches;
    private final ISystemConfigRepository systemConfig;
    private final IMemberRepository memberRepository;
    private final IClientInvoker clientInvoker;
    private final IServiceApiRepository serviceApiRepository;
    private final IFeatureRepository featureRepository;
    private final IExecutePointRepository executePointRepository;
    private final IExecuteTemplateRepository executeTemplateRepository;
    private final IBuildToolRepository buildToolRepository;

    public MicroserviceService(IMicroServiceRepository microServiceRepository, UniqueIdService uniqueIdService,
                               IAuthService authService, List<IGitRepositoryHandler> gitRepositories,
                               ISystemConfigRepository systemConfig, IPipelineRepository pipelineRepository,
                               ITestCaseRepository testCaseRepository, IMemberRepository memberRepository,
                               IClientInvoker clientInvoker, IServiceApiRepository serviceApiRepository,
                               IFeatureRepository featureRepository, IExecutePointRepository executePointRepository,
                               IExecuteTemplateRepository executeTemplateRepository, IBuildToolRepository buildToolRepository) {
        this.microServiceRepository = microServiceRepository;
        this.uniqueIdService = uniqueIdService;
        this.authService = authService;
        this.pipelineRepository = pipelineRepository;
        this.testCaseRepository = testCaseRepository;
        this.repositoryBranches = gitRepositories;
        this.systemConfig = systemConfig;
        this.memberRepository = memberRepository;
        this.clientInvoker = clientInvoker;
        this.serviceApiRepository = serviceApiRepository;
        this.featureRepository = featureRepository;
        this.executePointRepository = executePointRepository;
        this.executeTemplateRepository = executeTemplateRepository;
        this.buildToolRepository = buildToolRepository;
    }

    private IGitRepositoryHandler getRepositoryBranch(String type) {
        return repositoryBranches.stream().filter(repository -> Objects.equals(repository.gitType(), type)).findAny().orElse(null);
    }

    public PageSize<ServiceDto> getServices(Integer pageNo, Integer size, String name) {
        String currentUserId = authService.getCurrentUserId();
        List<ResourceMemberBO> resourceMembers = memberRepository.getByRelationMember(currentUserId, MemberType.SERVICE_MEMBER.getType());
        if (CollectionUtils.isEmpty(resourceMembers)) {
            return new PageSize<>();
        }

        List<String> serviceIds =
                resourceMembers.stream().map(ResourceMemberBO::getResourceId).collect(Collectors.toList());
        IPage<MicroserviceBO> page = microServiceRepository.getServices(pageNo, size, name, serviceIds);
        PageSize<ServiceDto> pageSize = new PageSize<>();
        if (CollectionUtils.isEmpty(page.getRecords())) {
            pageSize.setTotal(0);
            return pageSize;
        }

        List<ServiceDto> microservices = page.getRecords().stream().map(microservice -> {
            ServiceDto serviceDto = OrikaUtil.convert(microservice, ServiceDto.class);
            serviceDto.setServiceConfig(microservice.getServiceConfig());
            return serviceDto;
        }).collect(Collectors.toList());

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

        GitAccessInfo gitAccessInfo =
                Optional.ofNullable(serviceDto.getServiceConfig()).map(ServiceConfig::getGitAccessInfo).filter(access -> StringUtils.isNotBlank(access.getAccessToken())).orElseGet(systemConfig::getGitAccess);
        gitAccessInfo.setGitUrl(serviceDto.getGitUrl());
        repositoryBranch.checkRepository(gitAccessInfo);

        MicroserviceBO microserviceBO = OrikaUtil.convert(serviceDto, MicroserviceBO.class);
        microserviceBO.setServiceConfig(serviceDto.getServiceConfig());
        microserviceBO.setServiceId(uniqueIdService.getUniqueId());
        String currentUserId = authService.getCurrentUserId();
        return microServiceRepository.createService(currentUserId, microserviceBO);
    }

    private String chooseGitType(ServiceDto serviceDto) {
        if (Objects.isNull(serviceDto.getServiceConfig()) || Objects.isNull(serviceDto.getServiceConfig().getGitAccessInfo()) || StringUtils.isBlank(serviceDto.getServiceConfig().getGitAccessInfo().getAccessToken())) {
            return systemConfig.getGitAccess().getGitType();
        }
        return serviceDto.getServiceConfig().getGitAccessInfo().getGitType();
    }

    public String updateService(ServiceDto update) {
        MicroserviceBO microserviceBO = OrikaUtil.convert(update, MicroserviceBO.class);
        Optional.ofNullable(update.getServiceConfig()).ifPresent(microserviceBO::setServiceConfig);
        return microServiceRepository.updateService(microserviceBO) ? microserviceBO.getServiceId() : null;
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

    public MicroserviceBO queryServiceDetail(String serviceId) {
        return microServiceRepository.queryServiceDetail(serviceId);
    }

    public List<MicroserviceBO> getServices() {
        String currentUserId = authService.getCurrentUserId();
        return microServiceRepository.getUserRelatedServices(currentUserId).stream().sorted(Comparator.comparing(MicroserviceBO::getPriority).reversed()).collect(Collectors.toList());
    }

    public List<UserBO> queryServiceMembers(String serviceId) {
        return memberRepository.getResourceUserMembers(serviceId, MemberType.SERVICE_MEMBER.getType());
    }

    public Boolean addServiceMember(ServiceMemberDto serviceMemberDto) {
        ResourceMemberBO resourceMemberBO = new ResourceMemberBO();
        resourceMemberBO.setMemberType(MemberType.SERVICE_MEMBER.getType());
        resourceMemberBO.setResourceId(serviceMemberDto.getServiceId());
        resourceMemberBO.setRelationId(serviceMemberDto.getUserId());
        return memberRepository.addResourceMember(resourceMemberBO);
    }

    public Boolean deleteServiceMember(String serviceId, String userId) {
        return memberRepository.deleteResourceMember(serviceId, userId);
    }

    public ServiceStaticsDto getServiceStatics(String serviceId) {
        MicroserviceBO microserviceBO = microServiceRepository.queryServiceDetail(serviceId);
        if (Objects.isNull(microserviceBO)) {
            log.info("can not find service = {}", serviceId);
            throw new ApiException(ErrorCode.NOT_FOUND_SERVICE);
        }
        List<ServiceApiBO> serviceApiList =
                serviceApiRepository.getApiByService(serviceId).stream().filter(api -> Objects.equals(ApiType.API.getType(), api.getApiType())).collect(Collectors.toList());

        Map<Boolean, List<ServiceApiBO>> serviceApiPartMap = getServiceApiPartMap(serviceId, serviceApiList);
        Integer serviceApiCount = serviceApiList.size();
        return new ServiceStaticsDto(serviceApiCount, microserviceBO.getApiCoverage(), serviceApiPartMap.get(false));
    }

    public Map<Boolean, List<ServiceApiBO>> getServiceApiPartMap(String serviceId, List<ServiceApiBO> serviceApiList) {
        List<ExecuteTemplateBO> templates = getServiceAllExecuteTemplate(serviceId);

        List<String> templateApiList =
                templates.stream().map(template -> template.getMethod().toUpperCase() + "_" + template.getService()).map(uri -> uri.replace(WindyConstants.VARIABLE_CHAR, "")).collect(Collectors.toList());
        return serviceApiList.stream().collect(Collectors.partitioningBy(serviceApi -> templateApiList.stream().anyMatch(templateApi -> {
            int index = templateApi.indexOf("_");
            String method = templateApi.substring(0, index);
            String api = templateApi.substring(index + 1);
            if (!Objects.equals(method, serviceApi.getMethod().toUpperCase())) {
                return false;
            }
            return arePathsEqual(api, serviceApi.getResource());
        })));
    }

    private List<ExecuteTemplateBO> getServiceAllExecuteTemplate(String serviceId) {
        List<String> caseIds = testCaseRepository.getServiceCases(serviceId).stream().map(TestCaseBO::getTestCaseId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(caseIds)) {
            return Collections.emptyList();
        }
        List<String> featureIds =
                featureRepository.getFeatureByCases(caseIds).stream().filter(feature -> Objects.equals(feature.getFeatureType(), FeatureType.ITEM.getType())).map(FeatureInfoBO::getFeatureId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(featureIds)) {
            return Collections.emptyList();
        }

        List<ExecutePointBO> executePointList = executePointRepository.getPointsByFeatureIds(featureIds);
        List<String> templateIds = executePointList.stream().map(ExecutePointBO::getTemplateId).distinct().collect(Collectors.toList());
        List<ExecuteTemplateBO> templateList = executeTemplateRepository.getTemplateByIds(templateIds).stream()
                .filter(executeTemplate -> Objects.equals(executeTemplate.getInvokeType(), InvokerType.HTTP.getType())
                        || Objects.equals(executeTemplate.getInvokeType(),
                        InvokerType.RELATED_TEMPLATE.getType()))
                .collect(Collectors.toList());

        //将if和for包含的模版也统计
        List<ExecuteTemplateBO> specialTemplates =
                executePointList.stream().filter(executePointBO -> Objects.equals(executePointBO.getExecuteType(),
                        TemplateType.IF.getType()) || Objects.equals(executePointBO.getExecuteType(),
                        TemplateType.FOR.getType())).map(pointBO -> pointBO.getExecutorUnit().getExecutePoints()).flatMap(List::stream)
                .filter(Objects::nonNull).map(point -> {
                    ExecuteTemplateBO executeTemplateBO = new ExecuteTemplateBO();
                    executeTemplateBO.setService(point.getExecutorUnit().getService());
                    executeTemplateBO.setMethod(point.getExecutorUnit().getMethod());
                    return executeTemplateBO;
                }).collect(Collectors.toList());
        templateList.addAll(specialTemplates);
        return templateList;
    }


    public boolean arePathsEqual(String templatePath, String apiPath) {
        // 去掉路径前缀（例如 {host}/ 或 ${host}/）
        String path = "/";
        templatePath = StringUtils.removeStart(templatePath,
                StringUtils.substringBetween(templatePath, "{", "}") + path);
        templatePath = StringUtils.removeStart(templatePath,
                StringUtils.substringBetween(templatePath, "${", "}") + path);

        apiPath = StringUtils.removeStart(apiPath, StringUtils.substringBetween(apiPath, "{", "}") + path);
        apiPath = StringUtils.removeStart(apiPath, StringUtils.substringBetween(apiPath, "${", "}") + path);

        String variableName = "{var}";
        // 使用正则表达式将动态参数替换为统一的标记
        templatePath = templatePath.replaceAll("\\$\\{[^}]+}", variableName).replaceAll("\\{[^}]+}", variableName);
        apiPath = apiPath.replaceAll("\\$\\{[^}]+}", variableName).replaceAll("\\{[^}]+}", variableName);

        // 比较路径
        return templatePath.contains(apiPath);
    }

    public Boolean createBuildTool(SystemBuildDto systemBuildDto) {
        checkBuildTool(systemBuildDto);

        BuildToolBO buildToolBO = OrikaUtil.convert(systemBuildDto, BuildToolBO.class);
        buildToolBO.setToolId(uniqueIdService.getUniqueId());
        return buildToolRepository.saveBuildTool(buildToolBO);
    }

    private void checkBuildTool(SystemBuildDto systemBuildDto) {
        ToolVersionDto toolVersionDto = OrikaUtil.convert(systemBuildDto, ToolVersionDto.class);
        List<String> loadErrorClients = clientInvoker.loadBuildTool(toolVersionDto).stream()
                .filter(result -> !result.getSuccess()).map(ToolLoadResult::getNodeIP).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(loadErrorClients)){
            log.info("client load tool error list={}", loadErrorClients);
            throw new CommonException(ErrorCode.LOAD_CLIENT_BUILD_TOOL_ERROR, String.join(",", loadErrorClients));
        }
    }

    public Boolean updateBuildTool(SystemBuildDto systemBuildDto) {
        checkBuildTool(systemBuildDto);
        BuildToolBO buildToolBO = OrikaUtil.convert(systemBuildDto, BuildToolBO.class);
        return buildToolRepository.updateBuildTool(buildToolBO);
    }

    public Boolean deleteBuildTool(String toolId) {
        return buildToolRepository.deleteBuildTool(toolId);
    }

    public List<BuildToolBO> getToolVersions() {
        return buildToolRepository.getBuildToolList();
    }

    public SystemVersion getSystemVersion() {
        return new SystemVersion(consoleVersion);
    }
}
