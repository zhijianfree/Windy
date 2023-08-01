package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.dto.pipeline.SystemConfigDto;
import com.zj.domain.entity.vo.DefaultPipeline;
import com.zj.domain.entity.vo.GitAccessVo;
import com.zj.domain.entity.vo.ImageRepository;
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

  GitAccessVo getGitAccess();
  boolean updateGitAccess(GitAccessVo gitAccess);

  ImageRepository getRepository();

  boolean updateRepository(ImageRepository imageRepository);

  DefaultPipeline getDefaultPipeline();
}
