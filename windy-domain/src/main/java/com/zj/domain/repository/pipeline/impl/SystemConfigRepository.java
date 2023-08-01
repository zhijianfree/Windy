package com.zj.domain.repository.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.SystemConfigDto;
import com.zj.domain.entity.po.pipeline.SystemConfig;
import com.zj.domain.entity.vo.DefaultPipeline;
import com.zj.domain.entity.vo.GitAccessVo;
import com.zj.domain.entity.vo.ImageRepository;
import com.zj.domain.mapper.pipeline.SystemConfigMapper;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
@Repository
public class SystemConfigRepository extends ServiceImpl<SystemConfigMapper, SystemConfig> implements
    ISystemConfigRepository {

  public static final String GIT_ACCESS = "git_access";
  public static final String IMAGE_REPOSITORY = "image_repository";
  public static final String DEFAULT_PIPELINE = "default_pipeline";
  public static final Integer GLOBAL = 1;

  @Override
  public List<SystemConfigDto> getAllConfigs() {
    List<SystemConfig> systemConfigs = list();
    return OrikaUtil.convertList(systemConfigs, SystemConfigDto.class);
  }

  @Override
  public boolean saveConfig(SystemConfigDto systemConfigDto) {
    SystemConfig systemConfig = OrikaUtil.convert(systemConfigDto, SystemConfig.class);
    long dateNow = System.currentTimeMillis();
    systemConfig.setCreateTime(dateNow);
    systemConfig.setUpdateTime(dateNow);
    return save(systemConfig);
  }

  @Override
  public boolean updateConfig(SystemConfigDto systemConfigDto) {
    SystemConfig systemConfig = OrikaUtil.convert(systemConfigDto, SystemConfig.class);
    systemConfig.setUpdateTime(System.currentTimeMillis());
    return update(systemConfig, Wrappers.lambdaUpdate(SystemConfig.class)
        .eq(SystemConfig::getConfigId, systemConfig.getConfigId()));
  }

  @Override
  public boolean deleteConfig(String configId) {
    return remove(Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigId, configId));
  }

  @Override
  public SystemConfigDto getSystemConfig(String configId) {
    SystemConfig systemConfig = getOne(
        Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigId, configId));
    return OrikaUtil.convert(systemConfig, SystemConfigDto.class);
  }

  @Override
  public GitAccessVo getGitAccess() {
    SystemConfig systemConfig = getOne(
        Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigName, GIT_ACCESS));
    if (Objects.isNull(systemConfig)) {
      return new GitAccessVo();
    }
    return JSON.parseObject(systemConfig.getConfigDetail(), GitAccessVo.class);
  }

  @Override
  public boolean updateGitAccess(GitAccessVo gitAccess) {
    SystemConfig systemConfig = new SystemConfig();
    systemConfig.setConfigDetail(JSON.toJSONString(gitAccess));
    systemConfig.setUpdateTime(System.currentTimeMillis());
    SystemConfig config = getOne(Wrappers.lambdaUpdate(SystemConfig.class)
        .eq(SystemConfig::getConfigName, GIT_ACCESS));
    if (Objects.isNull(config)) {
      systemConfig.setConfigName(GIT_ACCESS);
      systemConfig.setConfigId(UUID.randomUUID().toString());
      systemConfig.setType(GLOBAL);
      systemConfig.setCreateTime(System.currentTimeMillis());
      return save(systemConfig);
    }
    return update(systemConfig,
        Wrappers.lambdaUpdate(SystemConfig.class).eq(SystemConfig::getConfigName, GIT_ACCESS));
  }

  @Override
  public ImageRepository getRepository() {
    SystemConfig systemConfig = getOne(
        Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigName, IMAGE_REPOSITORY));
    if (Objects.isNull(systemConfig)) {
      return new ImageRepository();
    }
    return JSON.parseObject(systemConfig.getConfigDetail(), ImageRepository.class);
  }

  @Override
  public boolean updateRepository(ImageRepository imageRepository) {
    SystemConfig systemConfig = new SystemConfig();
    systemConfig.setConfigDetail(JSON.toJSONString(imageRepository));
    systemConfig.setUpdateTime(System.currentTimeMillis());

    SystemConfig config = getOne(Wrappers.lambdaUpdate(SystemConfig.class)
        .eq(SystemConfig::getConfigName, IMAGE_REPOSITORY));
    if (Objects.isNull(config)) {
      systemConfig.setConfigName(IMAGE_REPOSITORY);
      systemConfig.setConfigId(UUID.randomUUID().toString());
      systemConfig.setType(GLOBAL);
      systemConfig.setCreateTime(System.currentTimeMillis());
      return save(systemConfig);
    }
    return update(systemConfig, Wrappers.lambdaUpdate(SystemConfig.class)
        .eq(SystemConfig::getConfigName, IMAGE_REPOSITORY));
  }

  @Override
  public DefaultPipeline getDefaultPipeline() {
    SystemConfig systemConfig = getOne(
        Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigName, DEFAULT_PIPELINE));
    if (Objects.isNull(systemConfig)) {
      return new DefaultPipeline();
    }
    return JSON.parseObject(systemConfig.getConfigDetail(), DefaultPipeline.class);
  }
}
