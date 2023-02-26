package com.zj.feature.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.feature.entity.dto.MicroserviceDTO;
import com.zj.feature.entity.dto.PageSize;
import com.zj.feature.entity.po.Microservice;
import com.zj.feature.mapper.MicroserviceMapper;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class MicroserviceService extends ServiceImpl<MicroserviceMapper, Microservice> {

  public PageSize<MicroserviceDTO> getServices(Integer pageNo, Integer size, String name) {
    IPage<Microservice> iPage = new Page<Microservice>(pageNo, size);
    LambdaQueryWrapper<Microservice> queryWrapper = Wrappers.lambdaQuery(Microservice.class);
    if (!StringUtils.isEmpty(name)) {
      queryWrapper.like(Microservice::getServiceName, name);
    }
    IPage<Microservice> page = page(iPage, queryWrapper);
    PageSize<MicroserviceDTO> pageSize = new PageSize<MicroserviceDTO>();
    if (CollectionUtils.isEmpty(page.getRecords())) {
      pageSize.setTotal(0);
      return pageSize;
    }

    List<MicroserviceDTO> microserviceDTOS = page.getRecords().stream().map(microservice -> {
      MicroserviceDTO microserviceDto = new MicroserviceDTO();
      BeanUtils.copyProperties(microservice, microserviceDto);
      return microserviceDto;
    }).collect(Collectors.toList());

    pageSize.setData(microserviceDTOS);
    pageSize.setTotal(page.getTotal());
    return pageSize;
  }

  public String createService(MicroserviceDTO microserviceDto) {
    Microservice microservice = new Microservice();
    BeanUtils.copyProperties(microserviceDto, microservice);
    microservice.setServiceId(UUID.randomUUID().toString().replace("-", ""));
    microservice.setOwner("admin");
    boolean save = save(microservice);
    return save ? microservice.getServiceId() : null;
  }

  public String updateService(MicroserviceDTO microserviceDto) {
    Microservice microservice = new Microservice();
    BeanUtils.copyProperties(microserviceDto, microservice);
    boolean update = update(microservice, Wrappers.lambdaUpdate(Microservice.class)
        .eq(Microservice::getServiceId, microservice.getServiceId()));
    return update ? microserviceDto.getServiceId() : null;
  }

  public Integer deleteService(String serviceId) {
    boolean remove = remove(
        Wrappers.lambdaUpdate(Microservice.class).eq(Microservice::getServiceId, serviceId));
    return remove ? 1 : 0;
  }

  public List<MicroserviceDTO> getServices() {
    return list().stream().map(microservice -> {
      MicroserviceDTO microserviceDto = new MicroserviceDTO();
      BeanUtils.copyProperties(microservice, microserviceDto);
      return microserviceDto;
    }).collect(Collectors.toList());
  }
}
