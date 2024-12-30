package com.zj.domain.repository.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.SystemConfigBO;
import com.zj.domain.entity.po.pipeline.SystemConfig;
import com.zj.domain.entity.vo.DefaultPipelineVo;
import com.zj.domain.entity.vo.ImageRepositoryVo;
import com.zj.domain.entity.vo.GenerateMavenConfigDto;
import com.zj.domain.mapper.pipeline.SystemConfigMapper;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
@Repository
public class SystemConfigRepository extends ServiceImpl<SystemConfigMapper, SystemConfig> implements
        ISystemConfigRepository {

    public static final String GIT_ACCESS = "git_access";
    public static final String IMAGE_REPOSITORY = "image_repository";
    public static final String MAVEN_REPOSITORY = "maven_repository";
    public static final String DEFAULT_PIPELINE = "default_pipeline";
    public static final Integer GLOBAL = 1;

    @Override
    public List<SystemConfigBO> getAllConfigs() {
        List<SystemConfig> systemConfigs = list();
        return OrikaUtil.convertList(systemConfigs, SystemConfigBO.class);
    }

    @Override
    public boolean saveConfig(SystemConfigBO systemConfigBO) {
        SystemConfig systemConfig = OrikaUtil.convert(systemConfigBO, SystemConfig.class);
        long dateNow = System.currentTimeMillis();
        systemConfig.setCreateTime(dateNow);
        systemConfig.setUpdateTime(dateNow);
        return save(systemConfig);
    }

    @Override
    public boolean updateConfig(SystemConfigBO systemConfigBO) {
        SystemConfig systemConfig = OrikaUtil.convert(systemConfigBO, SystemConfig.class);
        systemConfig.setUpdateTime(System.currentTimeMillis());
        return update(systemConfig, Wrappers.lambdaUpdate(SystemConfig.class)
                .eq(SystemConfig::getConfigId, systemConfig.getConfigId()));
    }

    @Override
    public boolean deleteConfig(String configId) {
        return remove(Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigId, configId));
    }

    @Override
    public SystemConfigBO getSystemConfig(String configId) {
        SystemConfig systemConfig = getOne(
                Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigId, configId));
        return OrikaUtil.convert(systemConfig, SystemConfigBO.class);
    }

    @Override
    public GitAccessInfo getGitAccess() {
        GitAccessInfo gitAccess = getGlobalConfig(GIT_ACCESS, GitAccessInfo.class);
        return Optional.ofNullable(gitAccess).orElse(new GitAccessInfo());
    }

    @Override
    public boolean updateGitAccess(GitAccessInfo gitAccess) {
        return updateGlobalConfig(JSON.toJSONString(gitAccess), GIT_ACCESS);
    }

    @Override
    public ImageRepositoryVo getImageRepository() {
        ImageRepositoryVo imageRepositoryVo = getGlobalConfig(IMAGE_REPOSITORY, ImageRepositoryVo.class);
        return Optional.ofNullable(imageRepositoryVo).orElse(new ImageRepositoryVo());
    }

    @Override
    public boolean updateImageRepository(ImageRepositoryVo imageRepositoryVo) {
        return updateGlobalConfig(JSON.toJSONString(imageRepositoryVo), IMAGE_REPOSITORY);
    }

    @Override
    public DefaultPipelineVo getDefaultPipeline() {
        DefaultPipelineVo defaultPipeline = getGlobalConfig(DEFAULT_PIPELINE, DefaultPipelineVo.class);
        return Optional.ofNullable(defaultPipeline).orElse(new DefaultPipelineVo());
    }

    @Override
    public boolean updateMavenConfig(GenerateMavenConfigDto mavenConfig) {
        return updateGlobalConfig(JSON.toJSONString(mavenConfig), MAVEN_REPOSITORY);
    }

    @Override
    public GenerateMavenConfigDto getMavenConfig() {
        GenerateMavenConfigDto mavenConfig = getGlobalConfig(MAVEN_REPOSITORY, GenerateMavenConfigDto.class);
        return Optional.ofNullable(mavenConfig).orElse(new GenerateMavenConfigDto());
    }

    private <T> T getGlobalConfig(String type, Class<T> cls) {
        SystemConfig systemConfig = getOne(
                Wrappers.lambdaQuery(SystemConfig.class).eq(SystemConfig::getConfigName, type));
        if (Objects.isNull(systemConfig)) {
            return null;
        }
        return JSON.parseObject(systemConfig.getConfigDetail(), cls);
    }

    private Boolean updateGlobalConfig(String mavenConfig, String configType) {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setConfigDetail(mavenConfig);
        systemConfig.setUpdateTime(System.currentTimeMillis());
        SystemConfig config = getOne(Wrappers.lambdaUpdate(SystemConfig.class)
                .eq(SystemConfig::getConfigName, configType));
        if (Objects.isNull(config)) {
            systemConfig.setConfigName(configType);
            systemConfig.setConfigId(UUID.randomUUID().toString());
            systemConfig.setType(GLOBAL);
            systemConfig.setCreateTime(System.currentTimeMillis());
            return save(systemConfig);
        }
        return update(systemConfig,
                Wrappers.lambdaUpdate(SystemConfig.class).eq(SystemConfig::getConfigName, configType));
    }
}
