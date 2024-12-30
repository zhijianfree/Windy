package com.zj.pipeline.service;

import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.adapter.invoker.IClientInvoker;
import com.zj.common.adapter.invoker.IMasterInvoker;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.ClientCollectDto;
import com.zj.common.entity.dto.MasterCollectDto;
import com.zj.domain.entity.bo.pipeline.SystemConfigBO;
import com.zj.domain.entity.vo.DefaultPipelineVo;
import com.zj.domain.entity.vo.ImageRepositoryVo;
import com.zj.domain.entity.vo.GenerateMavenConfigDto;
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


  public List<SystemConfigBO> listSystemConfigs() {
    return systemConfigRepository.getAllConfigs();
  }

  public String createSystemConfig(SystemConfigBO systemConfigBO) {
    systemConfigBO.setConfigId(uniqueIdService.getUniqueId());
    return systemConfigRepository.saveConfig(systemConfigBO) ? systemConfigBO.getConfigId()
        : null;
  }

  public Boolean updateSystemConfig(SystemConfigBO systemConfig) {
    return systemConfigRepository.updateConfig(systemConfig);
  }

  public Boolean deleteSystemConfig(String configId) {
    return systemConfigRepository.deleteConfig(configId);
  }

  public SystemConfigBO getSystemConfig(String configId) {
    return systemConfigRepository.getSystemConfig(configId);
  }

  public SystemMonitorDto getSystemMonitor() {
    SystemMonitorDto systemMonitorDto = new SystemMonitorDto();
    List<ClientCollectDto> clientMonitor = clientInvoker.requestClientMonitor();
    systemMonitorDto.setClients(clientMonitor);
    List<MasterCollectDto> masterMonitor = masterInvoker.requestMasterMonitor();
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
    return systemConfigRepository.getImageRepository();
  }

  public boolean updateRepository(ImageRepositoryVo repository) {
    return systemConfigRepository.updateImageRepository(repository);
  }

  public DefaultPipelineVo getDefaultPipeline() {
    return systemConfigRepository.getDefaultPipeline();
  }

  public Boolean updateMavenConfig(GenerateMavenConfigDto mavenConfig) {
    return systemConfigRepository.updateMavenConfig(mavenConfig);
  }

  public GenerateMavenConfigDto getMavenConfig() {
    return systemConfigRepository.getMavenConfig();
  }
}
