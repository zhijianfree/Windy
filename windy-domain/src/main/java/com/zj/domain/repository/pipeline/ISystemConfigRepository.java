package com.zj.domain.repository.pipeline;

import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.domain.entity.bo.pipeline.SystemConfigBO;
import com.zj.domain.entity.vo.DefaultPipelineVo;
import com.zj.domain.entity.vo.ImageRepositoryVo;
import com.zj.domain.entity.vo.MavenConfigVo;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface ISystemConfigRepository {

  List<SystemConfigBO> getAllConfigs();

  boolean saveConfig(SystemConfigBO systemConfigBO);

  boolean updateConfig(SystemConfigBO systemConfigBO);

  boolean deleteConfig(String configId);

  SystemConfigBO getSystemConfig(String configId);

  GitAccessInfo getGitAccess();
  boolean updateGitAccess(GitAccessInfo gitAccess);

  ImageRepositoryVo getRepository();

  boolean updateRepository(ImageRepositoryVo imageRepositoryVo);

  DefaultPipelineVo getDefaultPipeline();

  Boolean updateMavenConfig(MavenConfigVo mavenConfig);

  MavenConfigVo getMavenConfig();
}
