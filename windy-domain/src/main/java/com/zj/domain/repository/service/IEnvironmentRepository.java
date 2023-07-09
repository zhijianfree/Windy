package com.zj.domain.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.dto.service.DeployEnvironmentDto;

public interface IEnvironmentRepository {

  IPage<DeployEnvironmentDto> getEnvPage(Integer page, Integer size);

  Boolean createEnvironment(DeployEnvironmentDto deployEnvironment);

  Boolean updateEnvironment(DeployEnvironmentDto deployEnvironment);

  Boolean deleteEnvironment(String envId);

  DeployEnvironmentDto getEnvironment(String envId);
}
