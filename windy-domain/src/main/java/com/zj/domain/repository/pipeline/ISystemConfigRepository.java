package com.zj.domain.repository.pipeline;

import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.domain.entity.bo.pipeline.SystemConfigBO;
import com.zj.domain.entity.vo.DefaultPipelineVo;
import com.zj.domain.entity.vo.ImageRepositoryVo;
import com.zj.domain.entity.vo.GenerateMavenConfigDto;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface ISystemConfigRepository {

  /**
   * 获取所有配置
   * @return 配置列表
   */
  List<SystemConfigBO> getAllConfigs();

  /**
   * 保存配置
   * @param systemConfigBO 配置信息
   * @return 是否成功
   */
  boolean saveConfig(SystemConfigBO systemConfigBO);

  /**
   * 更新配置
   * @param systemConfigBO 配置信息
   * @return 是否成功
   */
  boolean updateConfig(SystemConfigBO systemConfigBO);

  /**
   * 删除配置
   * @param configId 配置ID
   * @return 是否成功
   */
  boolean deleteConfig(String configId);

  /**
   * 获取配置
   * @param configId 配置ID
   * @return 配置信息
   */
  SystemConfigBO getSystemConfig(String configId);

  /**
   * 获取Git访问配置
   * @return 系统Git配置信息
   */
  GitAccessInfo getGitAccess();

  /**
   * 更新Git访问配置
   * @param gitAccess Git配置信息
   * @return 是否成功
   */
  boolean updateGitAccess(GitAccessInfo gitAccess);

  /**
   * 获取镜像仓库配置
   * @return 镜像仓库配置信息
   */
  ImageRepositoryVo getImageRepository();

  /**
   * 更新镜像仓库配置
   * @param imageRepositoryVo 镜像仓库配置信息
   * @return 是否成功
   */
  boolean updateImageRepository(ImageRepositoryVo imageRepositoryVo);

  /**
   * 获取默认流水线配置
   * @return 默认流水线配置信息
   */
  DefaultPipelineVo getDefaultPipeline();

  /**
   * 更新二方包maven配置
   * @param mavenConfig maven配置信息
   * @return 是否成功
   */
  boolean updateMavenConfig(GenerateMavenConfigDto mavenConfig);

  /**
   * 获取二方包maven配置
   * @return maven配置信息
   */
  GenerateMavenConfigDto getMavenConfig();
}
