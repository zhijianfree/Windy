package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
public class SystemConfigService {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    public List<SystemConfigDto> listSystemConfigs() {
        List<SystemConfig> systemConfigs = systemConfigMapper.selectList(Wrappers.<SystemConfig>emptyWrapper());
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
        int result = systemConfigMapper.insert(systemConfig);

        if (result > 0) {
            return systemConfig.getConfigId();
        }

        return null;
    }

    public String updateSystemConfig(SystemConfigDto systemConfigDto) {
        SystemConfig systemConfig = SystemConfigDto.toSystemConfig(systemConfigDto);
        systemConfig.setUpdateTime(System.currentTimeMillis());

        int result = systemConfigMapper.update(systemConfig, Wrappers.lambdaUpdate(SystemConfig.class).eq(SystemConfig::getConfigId, systemConfig.getConfigId()));
        if (result > 0) {
            return systemConfig.getConfigId();
        }

        return null;
    }

    public Integer deleteSystemConfig(String configId) {
        return systemConfigMapper.delete(Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigId, configId));
    }

    public SystemConfigDto getSystemConfig(String configId) {
        SystemConfig systemConfig = systemConfigMapper.selectOne(Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigId, configId));
        return SystemConfigDto.toSystemConfigDto(systemConfig);
    }
}
