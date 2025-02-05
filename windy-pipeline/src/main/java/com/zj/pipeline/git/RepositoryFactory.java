package com.zj.pipeline.git;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.adapter.git.IGitRepositoryHandler;
import com.zj.common.entity.pipeline.ServiceConfig;
import com.zj.common.entity.pipeline.ServiceContext;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/3/10
 */

@Slf4j
@Component
public class RepositoryFactory {

  private final IMicroServiceRepository serviceRepository;
  private final ISystemConfigRepository systemConfigRepository;
  private final Map<String, IGitRepositoryHandler> repositoryHandlerMap;

  public RepositoryFactory(IMicroServiceRepository serviceRepository, ISystemConfigRepository systemConfigRepository, List<IGitRepositoryHandler> repositories) {
    this.serviceRepository = serviceRepository;
    this.systemConfigRepository = systemConfigRepository;
    repositoryHandlerMap = repositories.stream().collect(Collectors.toMap(IGitRepositoryHandler::gitType,
            handler -> handler));
  }

  public IGitRepositoryHandler getRepository(String gitType) {
    return repositoryHandlerMap.get(gitType);
  }

  public GitAccessInfo getServiceRepositoryAccessInfo(String serviceId) {
    MicroserviceBO service = checkServiceExist(serviceId);
    GitAccessInfo gitAccessInfo = Optional.ofNullable(service.getServiceConfig())
            .map(ServiceConfig::getGitAccessInfo).filter(access -> StringUtils.isNotBlank(access.getAccessToken()))
            .orElseGet(systemConfigRepository::getGitAccess);
    ServiceContext serviceContext = service.getServiceConfig().getServiceContext();
    if (Objects.nonNull(serviceContext)) {
      gitAccessInfo.setMainBranch(serviceContext.getMainBranch());
    }
    gitAccessInfo.setGitUrl(service.getGitUrl());
    return gitAccessInfo;
  }

  private MicroserviceBO checkServiceExist(String serviceId) {
    MicroserviceBO serviceDetail = serviceRepository.queryServiceDetail(serviceId);
    if (Objects.isNull(serviceDetail)) {
      log.warn("can not find serviceId ={}", serviceId);
      throw new ApiException(ErrorCode.NOT_FOUND_SERVICE);
    }

    return serviceDetail;
  }
}
