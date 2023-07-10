package com.zj.domain.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.dto.service.DeployEnvironmentDto;
import java.util.List;

public interface IEnvironmentRepository {

  IPage<DeployEnvironmentDto> getEnvPage(Integer page, Integer size, String name);

  Boolean createEnvironment(DeployEnvironmentDto deployEnvironment);

  Boolean updateEnvironment(DeployEnvironmentDto deployEnvironment);

  Boolean deleteEnvironment(String envId);

  DeployEnvironmentDto getEnvironment(String envId);

  List<DeployEnvironmentDto> getAvailableEnvs();
}
