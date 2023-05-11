package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.pipeline.entity.dto.SystemConfigDto;
import com.zj.pipeline.entity.po.NodeParamConfig;
import com.zj.pipeline.entity.po.SystemConfig;
import com.zj.pipeline.mapper.NodeParamConfigMapper;
import com.zj.pipeline.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SystemConfigService extends ServiceImpl<SystemConfigMapper, SystemConfig> {

    public List<SystemConfigDto> listSystemConfigs() {
        List<SystemConfig> systemConfigs = list(Wrappers.<SystemConfig>emptyWrapper());
        if (CollectionUtils.isEmpty(systemConfigs)) {
            return Collections.emptyList();
        }

        return systemConfigs.stream().map(SystemConfigDto::toSystemConfigDto).collect(Collectors.toList());
    }

    public String createSystemConfig(SystemConfigDto systemConfigDto) {
        SystemConfig systemConfig = SystemConfigDto.toSystemConfig(systemConfigDto);
        systemConfig.setConfigId(UUID.randomUUID().toString());
        systemConfig.setCreateTime(System.currentTimeMillis());
        systemConfig.setUpdateTime(System.currentTimeMillis());
        return save(systemConfig) ? systemConfig.getConfigId() : null;
    }

    public String updateSystemConfig(SystemConfigDto systemConfigDto) {
        SystemConfig systemConfig = SystemConfigDto.toSystemConfig(systemConfigDto);
        systemConfig.setUpdateTime(System.currentTimeMillis());

        boolean result = update(systemConfig, Wrappers.lambdaUpdate(SystemConfig.class).eq(SystemConfig::getConfigId, systemConfig.getConfigId()));
        return result ? systemConfig.getConfigId() : null;
    }

    public Boolean deleteSystemConfig(String configId) {
        return remove(Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigId, configId));
    }

    public SystemConfigDto getSystemConfig(String configId) {
        SystemConfig systemConfig = getOne(Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigId, configId));
        return SystemConfigDto.toSystemConfigDto(systemConfig);
    }
}
