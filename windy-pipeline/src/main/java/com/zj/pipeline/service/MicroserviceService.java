package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.pipeline.entity.dto.MicroserviceDto;
import com.zj.pipeline.entity.po.Microservice;
import com.zj.pipeline.git.IRepositoryBranch;
import com.zj.pipeline.mapper.MicroserviceMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MicroserviceService extends ServiceImpl<MicroserviceMapper, Microservice> {

  @Autowired
  private IRepositoryBranch repositoryBranch;

  public List<MicroserviceDto> getServices() {
    List<Microservice> microservices = list(Wrappers.emptyWrapper());
    if (CollectionUtils.isEmpty(microservices)) {
      return Collections.emptyList();
    }

    return microservices.stream().map(microservice -> {
      MicroserviceDto microserviceDto = new MicroserviceDto();
      BeanUtils.copyProperties(microservice, microserviceDto);
      return microserviceDto;
    }).collect(Collectors.toList());
  }

  public String createService(MicroserviceDto microserviceDto) {
    Microservice microservice = new Microservice();
    BeanUtils.copyProperties(microserviceDto, microservice);
    microservice.setServiceId(UUID.randomUUID().toString());
    return save(microservice) ? microservice.getServiceId() : "";
  }

  public Microservice getServiceDetail(String serviceId) {
    return getOne(
        Wrappers.lambdaQuery(Microservice.class).eq(Microservice::getServiceId, serviceId));
  }

  public MicroserviceDto queryServiceDetail(String serviceId) {
    Microservice microservice = getOne(
        Wrappers.lambdaQuery(Microservice.class).eq(Microservice::getServiceId, serviceId));
    MicroserviceDto microserviceDto = new MicroserviceDto();
    BeanUtils.copyProperties(microservice, microserviceDto);
    return microserviceDto;
  }

  public List<String> getServiceBranch(String serviceId) {
    MicroserviceDto serviceDetail = queryServiceDetail(serviceId);
    return repositoryBranch.listBranch(serviceDetail.getServiceName());
  }
}
