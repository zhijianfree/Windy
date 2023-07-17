package com.zj.master.service;

import com.zj.domain.entity.dto.feature.PluginInfoDto;
import com.zj.domain.repository.feature.IPluginRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/7/14
 */
@Slf4j
@Service
public class PluginsService {

  private IPluginRepository pluginRepository;

  public PluginsService(IPluginRepository pluginRepository) {
    this.pluginRepository = pluginRepository;
  }

  public List<PluginInfoDto> getPlugins() {
    return pluginRepository.getAllPlugins();
  }
}
