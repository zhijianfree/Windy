package com.zj.domain.repository.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.service.DeployEnvironmentBO;
import java.util.List;

public interface IEnvironmentRepository {

  /**
   * 分页获取环境列表
   * @param page 页码
   * @param size 每页数量
   * @param name 环境名称
   * @return 环境列表
   */
  IPage<DeployEnvironmentBO> getEnvPage(Integer page, Integer size, String name);

  /**
   * 创建环境
   * @param deployEnvironment 环境信息
   * @return 是否成功
   */
  Boolean createEnvironment(DeployEnvironmentBO deployEnvironment);

  /**
   * 更新环境
   * @param deployEnvironment 环境信息
   * @return 是否成功
   */
  Boolean updateEnvironment(DeployEnvironmentBO deployEnvironment);

  /**
   * 删除环境
   * @param envId 环境ID
   * @return 是否成功
   */
  Boolean deleteEnvironment(String envId);

  /**
   * 获取环境
   * @param envId 环境ID
   * @return 环境信息
   */
  DeployEnvironmentBO getEnvironment(String envId);

  /**
   * 获取可用环境
   * @return 环境列表
   */
  List<DeployEnvironmentBO> getAvailableEnvs();
}
