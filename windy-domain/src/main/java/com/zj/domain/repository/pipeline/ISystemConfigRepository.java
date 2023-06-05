package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.dto.pipeline.SystemConfigDto;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface ISystemConfigRepository {

  List<SystemConfigDto> getAllConfigs();

  boolean saveConfig(SystemConfigDto systemConfigDto);

  boolean updateConfig(SystemConfigDto systemConfigDto);

  boolean deleteConfig(String configId);

  SystemConfigDto getSystemConfig(String configId);
}
