package com.zj.domain.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.service.DeployEnvironmentBO;
import java.util.List;

public interface IEnvironmentRepository {

  IPage<DeployEnvironmentBO> getEnvPage(Integer page, Integer size, String name);

  Boolean createEnvironment(DeployEnvironmentBO deployEnvironment);

  Boolean updateEnvironment(DeployEnvironmentBO deployEnvironment);

  Boolean deleteEnvironment(String envId);

  DeployEnvironmentBO getEnvironment(String envId);

  List<DeployEnvironmentBO> getAvailableEnvs();
}
