package com.zj.service.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.repository.service.IMicroServiceRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class MicroserviceService {

  @Autowired
  private IMicroServiceRepository microServiceRepository;
  
  @Autowired
  private UniqueIdService uniqueIdService;

  public PageSize<MicroserviceDto> getServices(Integer pageNo, Integer size, String name) {
    IPage<MicroserviceDto> page = microServiceRepository.getServices(pageNo, size, name);
    PageSize<MicroserviceDto> pageSize = new PageSize<>();
    if (CollectionUtils.isEmpty(page.getRecords())) {
      pageSize.setTotal(0);
      return pageSize;
    }

    List<MicroserviceDto> MicroserviceDtoS = page.getRecords().stream()
        .map(microservice -> OrikaUtil.convert(microservice, MicroserviceDto.class))
        .collect(Collectors.toList());

    pageSize.setData(MicroserviceDtoS);
    pageSize.setTotal(page.getTotal());
    return pageSize;
  }

  public String createService(MicroserviceDto microserviceDto) {
    microserviceDto.setServiceId(uniqueIdService.getUniqueId());
    return microServiceRepository.createService(microserviceDto);
  }

  public String updateService(MicroserviceDto microserviceDto) {
    return microServiceRepository.updateService(microserviceDto);
  }

  public Boolean deleteService(String serviceId) {
    return microServiceRepository.deleteService(serviceId);
  }

  public MicroserviceDto queryServiceDetail(String serviceId) {
    return microServiceRepository.queryServiceDetail(serviceId);
  }

  public MicroserviceDto queryServiceByName(String serviceName) {
    return microServiceRepository.queryServiceByName(serviceName);
  }

  public List<MicroserviceDto> getServices() {
    return microServiceRepository.getServices();
  }
}
