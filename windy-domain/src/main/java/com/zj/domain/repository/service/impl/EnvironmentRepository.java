package com.zj.domain.repository.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.service.DeployEnvironmentBO;
import com.zj.domain.entity.enums.SourceStatus;
import com.zj.domain.entity.po.service.DeployEnvironment;
import com.zj.domain.mapper.service.EnvironmentMapper;
import com.zj.domain.repository.service.IEnvironmentRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class EnvironmentRepository extends
    ServiceImpl<EnvironmentMapper, DeployEnvironment> implements IEnvironmentRepository {

  @Override
  public IPage<DeployEnvironmentBO> getEnvPage(Integer page, Integer size, String name) {
    Page<DeployEnvironment> environmentPage = page(new Page<>(page, size),
        Wrappers.lambdaQuery(DeployEnvironment.class).like(DeployEnvironment::getEnvName, name));

    IPage<DeployEnvironmentBO> envPage = new Page<>();
    envPage.setTotal(environmentPage.getTotal());
    envPage.setRecords(
        OrikaUtil.convertList(environmentPage.getRecords(), DeployEnvironmentBO.class));
    return envPage;
  }

  @Override
  public Boolean createEnvironment(DeployEnvironmentBO deployEnvironment) {
    DeployEnvironment environment = OrikaUtil.convert(deployEnvironment, DeployEnvironment.class);
    long dateNow = System.currentTimeMillis();
    environment.setCreateTime(dateNow);
    environment.setUpdateTime(dateNow);
    return save(environment);
  }

  @Override
  public Boolean updateEnvironment(DeployEnvironmentBO deployEnvironment) {
    DeployEnvironment environment = OrikaUtil.convert(deployEnvironment, DeployEnvironment.class);
    environment.setUpdateTime(System.currentTimeMillis());
    return update(environment, Wrappers.lambdaQuery(DeployEnvironment.class)
        .eq(DeployEnvironment::getEnvId, environment.getEnvId()));
  }

  @Override
  public Boolean deleteEnvironment(String envId) {
    return remove(
        Wrappers.lambdaQuery(DeployEnvironment.class).eq(DeployEnvironment::getEnvId, envId));
  }

  @Override
  public DeployEnvironmentBO getEnvironment(String envId) {
    DeployEnvironment environment = getOne(
        Wrappers.lambdaQuery(DeployEnvironment.class).eq(DeployEnvironment::getEnvId, envId));
    return OrikaUtil.convert(environment, DeployEnvironmentBO.class);
  }

  @Override
  public List<DeployEnvironmentBO> getAvailableEnvs() {
    List<DeployEnvironment> environments = list(
        Wrappers.lambdaQuery(DeployEnvironment.class).eq(DeployEnvironment::getEnvStatus,
            SourceStatus.AVAILABLE.getType()));
    return OrikaUtil.convertList(environments, DeployEnvironmentBO.class);
  }
}
