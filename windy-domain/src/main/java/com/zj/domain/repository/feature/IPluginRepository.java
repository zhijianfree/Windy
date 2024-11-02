package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.PluginInfoBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/7/14
 */
public interface IPluginRepository {

  List<PluginInfoBO> getAllPlugins();

  boolean addPlugin(PluginInfoBO pluginInfo);

  PluginInfoBO getPlugin(String pluginId);

  boolean deletePlugin(String pluginId);

  void updatePluginStatus(String pluginId);
}
