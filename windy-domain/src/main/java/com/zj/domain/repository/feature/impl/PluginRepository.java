package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.PluginInfoDto;
import com.zj.domain.entity.enums.SourceStatus;
import com.zj.domain.entity.po.feature.PluginInfo;
import com.zj.domain.mapper.feeature.PluginInfoMapper;
import com.zj.domain.repository.feature.IPluginRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PluginRepository extends ServiceImpl<PluginInfoMapper, PluginInfo> implements
    IPluginRepository {

  @Override
  public List<PluginInfoDto> getAllPlugins() {
    List<PluginInfo> list = list(Wrappers.lambdaQuery(PluginInfo.class)
        .eq(PluginInfo::getStatus, SourceStatus.AVAILABLE.getType()));
    return OrikaUtil.convertList(list, PluginInfoDto.class);
  }

  @Override
  public boolean addPlugin(PluginInfoDto pluginInfo) {
    PluginInfo plugin = OrikaUtil.convert(pluginInfo, PluginInfo.class);
    plugin.setCreateTime(System.currentTimeMillis());
    plugin.setUpdateTime(System.currentTimeMillis());
    return save(plugin);
  }

  @Override
  public boolean deletePlugin(String pluginId) {
    return remove(Wrappers.lambdaQuery(PluginInfo.class).eq(PluginInfo::getPluginId, pluginId));
  }

  @Override
  public void updatePluginStatus(String pluginId) {
    PluginInfo pluginInfo = new PluginInfo();
    pluginInfo.setPluginId(pluginId);
    pluginInfo.setUpdateTime(System.currentTimeMillis());
    pluginInfo.setStatus(SourceStatus.AVAILABLE.getType());
    update(pluginInfo,
        Wrappers.lambdaQuery(PluginInfo.class).eq(PluginInfo::getPluginId, pluginId));
  }

  @Override
  public PluginInfoDto getPlugin(String pluginId) {
    PluginInfo pluginInfo = getOne(
        Wrappers.lambdaQuery(PluginInfo.class).eq(PluginInfo::getPluginId, pluginId));
    return OrikaUtil.convert(pluginInfo, PluginInfoDto.class);
  }
}
