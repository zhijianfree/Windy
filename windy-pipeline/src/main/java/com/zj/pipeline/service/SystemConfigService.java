package com.zj.pipeline.service;

import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.adapter.invoker.IClientInvoker;
import com.zj.common.adapter.invoker.IMasterInvoker;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.ClientCollect;
import com.zj.common.entity.dto.MasterCollect;
import com.zj.domain.entity.bo.pipeline.SystemConfigDto;
import com.zj.domain.entity.vo.DefaultPipelineVo;
import com.zj.domain.entity.vo.ImageRepositoryVo;
import com.zj.domain.entity.vo.MavenConfigVo;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.pipeline.entity.dto.SystemMonitorDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

  private final ISystemConfigRepository systemConfigRepository;
  private final UniqueIdService uniqueIdService;
  private final IMasterInvoker masterInvoker;
  private final IClientInvoker clientInvoker;

  public SystemConfigService(ISystemConfigRepository systemConfigRepository,
                             UniqueIdService uniqueIdService, IMasterInvoker masterInvoker, IClientInvoker clientInvoker) {
    this.systemConfigRepository = systemConfigRepository;
    this.uniqueIdService = uniqueIdService;
    this.masterInvoker = masterInvoker;
    this.clientInvoker = clientInvoker;
  }


  public List<SystemConfigDto> listSystemConfigs() {
    return systemConfigRepository.getAllConfigs();
  }

  public String createSystemConfig(SystemConfigDto systemConfigDto) {
    systemConfigDto.setConfigId(uniqueIdService.getUniqueId());
    return systemConfigRepository.saveConfig(systemConfigDto) ? systemConfigDto.getConfigId()
        : null;
  }

  public Boolean updateSystemConfig(SystemConfigDto systemConfig) {
    return systemConfigRepository.updateConfig(systemConfig);
  }

  public Boolean deleteSystemConfig(String configId) {
    return systemConfigRepository.deleteConfig(configId);
  }

  public SystemConfigDto getSystemConfig(String configId) {
    return systemConfigRepository.getSystemConfig(configId);
  }

  public SystemMonitorDto getSystemMonitor() {
    SystemMonitorDto systemMonitorDto = new SystemMonitorDto();
    List<ClientCollect> clientMonitor = clientInvoker.requestClientMonitor();
    systemMonitorDto.setClients(clientMonitor);
    List<MasterCollect> masterMonitor = masterInvoker.requestMasterMonitor();
    systemMonitorDto.setMasters(masterMonitor);
    return systemMonitorDto;
  }

  public GitAccessInfo getGitConfig() {
    return systemConfigRepository.getGitAccess();
  }

  public boolean updateGitConfig(GitAccessInfo gitAccessInfo) {
    return systemConfigRepository.updateGitAccess(gitAccessInfo);
  }

  public ImageRepositoryVo getImageRepository() {
    return systemConfigRepository.getRepository();
  }

  public boolean updateRepository(ImageRepositoryVo repository) {
    return systemConfigRepository.updateRepository(repository);
  }

  public DefaultPipelineVo getDefaultPipeline() {
    return systemConfigRepository.getDefaultPipeline();
  }

  public Boolean updateMavenConfig(MavenConfigVo mavenConfig) {
    return systemConfigRepository.updateMavenConfig(mavenConfig);
  }

  public MavenConfigVo getMavenConfig() {
    return systemConfigRepository.getMavenConfig();
  }
}
