package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.SystemConfigDto;
import com.zj.domain.entity.po.pipeline.SystemConfig;
import com.zj.domain.mapper.pipeline.SystemConfigMapper;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author falcon
 * @since 2023/5/19
 */
@Repository
public class SystemConfigRepository extends ServiceImpl<SystemConfigMapper, SystemConfig> implements
    ISystemConfigRepository {

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

}
