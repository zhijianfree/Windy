package com.zj.domain.repository.feature;

import com.zj.domain.entity.dto.feature.PluginInfoDto;
import java.util.List;

/**
 * @author falcon
 * @since 2023/7/14
 */
public interface IPluginRepository {

  List<PluginInfoDto> getAllPlugins();

  boolean addPlugin(PluginInfoDto pluginInfo);

  PluginInfoDto getPlugin(String pluginId);

  boolean deletePlugin(String pluginId);
}
