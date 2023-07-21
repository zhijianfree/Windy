package com.zj.service.service;

import com.zj.common.model.ClientCollect;
import com.zj.common.model.MasterCollect;
import com.zj.common.monitor.RequestProxy;
import com.zj.domain.repository.feature.IFeatureRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.service.entity.StaticsCount;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * @author falcon
 * @since 2023/7/21
 */
@Service
public class MainPageService {

  private IMicroServiceRepository serviceRepository;
  private IPipelineRepository pipelineRepository;
  private IFeatureRepository featureRepository;
  private RequestProxy requestProxy;

  public MainPageService(IMicroServiceRepository serviceRepository,
      IPipelineRepository pipelineRepository, IFeatureRepository featureRepository,
      RequestProxy requestProxy) {
    this.serviceRepository = serviceRepository;
    this.pipelineRepository = pipelineRepository;
    this.featureRepository = featureRepository;
    this.requestProxy = requestProxy;
  }

  public StaticsCount queryStaticsCount() {
    StaticsCount staticsCount = new StaticsCount();
    staticsCount.setServices(serviceRepository.countAll());
    staticsCount.setPipelines(pipelineRepository.countAll());
    staticsCount.setFeatures(featureRepository.countAll());
    return staticsCount;
  }

  public void getSystemMonitor() {
    List<ClientCollect> clientMonitor = requestProxy.requestClientMonitor();

    List<MasterCollect> masterMonitor = requestProxy.requestMasterMonitor();
  }
}
