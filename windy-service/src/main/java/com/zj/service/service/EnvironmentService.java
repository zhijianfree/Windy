package com.zj.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.service.DeployEnvironmentDto;
import com.zj.domain.repository.service.IEnvironmentRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentService {

  private IEnvironmentRepository repository;
  private UniqueIdService uniqueIdService;

  public EnvironmentService(IEnvironmentRepository repository, UniqueIdService uniqueIdService) {
    this.repository = repository;
    this.uniqueIdService = uniqueIdService;
  }

  public PageSize<DeployEnvironmentDto> getEnvironments(Integer page, Integer size) {
    IPage<DeployEnvironmentDto> envPage = repository.getEnvPage(page, size);
    if (CollectionUtils.isEmpty(envPage.getRecords())) {
      return new PageSize<>();
    }

    PageSize<DeployEnvironmentDto> pageSize = new PageSize<>();
    pageSize.setData(envPage.getRecords());
    pageSize.setTotal(envPage.getTotal());
    return pageSize;
  }

  public Boolean createEnvironment(DeployEnvironmentDto deployEnvironment) {
    deployEnvironment.setEnvId(uniqueIdService.getUniqueId());
    return repository.createEnvironment(deployEnvironment);
  }

  public Boolean updateEnvironment(DeployEnvironmentDto deployEnvironment) {
    return repository.updateEnvironment(deployEnvironment);
  }

  public Boolean deleteEnvironment(String envId) {
    return repository.deleteEnvironment(envId);
  }

  public DeployEnvironmentDto getEnvironment(String envId) {
    return repository.getEnvironment(envId);
  }
}
