package com.zj.master.service;

import com.zj.domain.entity.bo.feature.PluginInfoBO;
import com.zj.domain.repository.feature.IPluginRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/7/14
 */
@Slf4j
@Service
public class PluginsService {

  private final IPluginRepository pluginRepository;

  public PluginsService(IPluginRepository pluginRepository) {
    this.pluginRepository = pluginRepository;
  }

  public List<PluginInfoBO> getPlugins() {
    return pluginRepository.getAllPlugins();
  }
}
