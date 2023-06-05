package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.SystemConfigDto;
import com.zj.domain.entity.po.pipeline.SystemConfig;
import com.zj.domain.mapper.pipeline.SystemConfigMapper;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SystemConfigService {

  @Autowired
  private ISystemConfigRepository systemConfigRepository;

  @Autowired
  private UniqueIdService uniqueIdService;


  public List<SystemConfigDto> listSystemConfigs() {
    return systemConfigRepository.getAllConfigs();
  }

  public String createSystemConfig(SystemConfigDto systemConfigDto) {
    systemConfigDto.setConfigId(uniqueIdService.getUniqueId());
    return systemConfigRepository.saveConfig(systemConfigDto) ? systemConfigDto.getConfigId()
        : null;
  }

  public String updateSystemConfig(SystemConfigDto systemConfig) {
    return systemConfigRepository.updateConfig(systemConfig) ? systemConfig.getConfigId() : null;
  }

  public Boolean deleteSystemConfig(String configId) {
    return systemConfigRepository.deleteConfig(configId);
  }

  public SystemConfigDto getSystemConfig(String configId) {
    return systemConfigRepository.getSystemConfig(configId);
  }
}
