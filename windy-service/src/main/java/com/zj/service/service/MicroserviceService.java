package com.zj.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.git.IRepositoryBranch;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.TestCaseDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.entity.vo.GitAccessVo;
import com.zj.domain.repository.feature.ITestCaseRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.apache.commons.collections4.CollectionUtils;

@Service
public class MicroserviceService {

  private final IMicroServiceRepository microServiceRepository;
  private final UniqueIdService uniqueIdService;
  private final IPipelineRepository pipelineRepository;

  private final ITestCaseRepository testCaseRepository;
  private final IRepositoryBranch repositoryBranch;

  public MicroserviceService(IMicroServiceRepository microServiceRepository,
      UniqueIdService uniqueIdService, List<IRepositoryBranch> gitRepositories,
      ISystemConfigRepository systemConfig, IPipelineRepository pipelineRepository,
      ITestCaseRepository testCaseRepository) {
    this.microServiceRepository = microServiceRepository;
    this.uniqueIdService = uniqueIdService;
    this.pipelineRepository = pipelineRepository;
    this.testCaseRepository = testCaseRepository;
    GitAccessVo gitAccess = systemConfig.getGitAccess();
    this.repositoryBranch = gitRepositories.stream()
        .filter(repository -> Objects.equals(repository.gitType(), gitAccess.getGitType()))
        .findAny().orElse(null);
  }

  public PageSize<MicroserviceDto> getServices(Integer pageNo, Integer size, String name) {
    IPage<MicroserviceDto> page = microServiceRepository.getServices(pageNo, size, name);
    PageSize<MicroserviceDto> pageSize = new PageSize<>();
    if (CollectionUtils.isEmpty(page.getRecords())) {
      pageSize.setTotal(0);
      return pageSize;
    }

    List<MicroserviceDto> microservices = page.getRecords().stream()
        .map(microservice -> OrikaUtil.convert(microservice, MicroserviceDto.class))
        .collect(Collectors.toList());

    pageSize.setData(microservices);
    pageSize.setTotal(page.getTotal());
    return pageSize;
  }

  public String createService(MicroserviceDto microserviceDto) {
    if (Objects.isNull(repositoryBranch)) {
      throw new ApiException(ErrorCode.NOT_FIND_REPO_CONFIG);
    }

    repositoryBranch.checkRepository(microserviceDto.getServiceName());

    microserviceDto.setServiceId(uniqueIdService.getUniqueId());
    return microServiceRepository.createService(microserviceDto);
  }

  public String updateService(MicroserviceDto microserviceDto) {
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
    return microServiceRepository.getServices().stream()
        .sorted(Comparator.comparing(MicroserviceDto::getPriority).reversed()).collect(
            Collectors.toList());
  }
}
