package com.zj.pipeline.service;

import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.ClientCollect;
import com.zj.common.model.MasterCollect;
import com.zj.common.monitor.RequestProxy;
import com.zj.domain.entity.dto.pipeline.SystemConfigDto;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.pipeline.entity.dto.SystemMonitorDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

  private ISystemConfigRepository systemConfigRepository;
  private UniqueIdService uniqueIdService;
  private RequestProxy requestProxy;

  public SystemConfigService(ISystemConfigRepository systemConfigRepository,
      UniqueIdService uniqueIdService, RequestProxy requestProxy) {
    this.systemConfigRepository = systemConfigRepository;
    this.uniqueIdService = uniqueIdService;
    this.requestProxy = requestProxy;
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
    List<ClientCollect> clientMonitor = requestProxy.requestClientMonitor();
    systemMonitorDto.setClients(clientMonitor);
    List<MasterCollect> masterMonitor = requestProxy.requestMasterMonitor();
    systemMonitorDto.setMasters(masterMonitor);
    return systemMonitorDto;
  }
}
