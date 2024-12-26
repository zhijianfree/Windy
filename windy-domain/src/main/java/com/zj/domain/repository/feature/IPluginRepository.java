package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.PluginInfoBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/7/14
 */
public interface IPluginRepository {

  /**
   * 获取所有插件
   * @return 插件列表
   */
  List<PluginInfoBO> getAllPlugins();

  /**
   * 添加插件
   * @param pluginInfo 插件信息
   * @return 是否成功
   */
  boolean addPlugin(PluginInfoBO pluginInfo);

  /**
   * 删除插件
   * @param pluginId 插件ID
   * @return 是否成功
   */
  boolean deletePlugin(String pluginId);

  /**
   * 启用插件
   * @param pluginId 插件ID
   * @return 是否成功
   */
  boolean enablePluginStatus(String pluginId);
}
