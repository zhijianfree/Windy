package com.zj.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.model.PageSize;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.service.entity.dto.MicroserviceDTO;
import com.zj.service.entity.po.Microservice;
import com.zj.service.mapper.MicroserviceMapper;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class MicroserviceService extends ServiceImpl<MicroserviceMapper, Microservice> {

  @Autowired
  private UniqueIdService uniqueIdService;

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

    List<MicroserviceDTO> microserviceDTOS = page.getRecords().stream()
        .map(microservice -> OrikaUtil.convert(microservice, MicroserviceDTO.class))
        .collect(Collectors.toList());

    pageSize.setData(microserviceDTOS);
    pageSize.setTotal(page.getTotal());
    return pageSize;
  }

  public String createService(MicroserviceDTO microserviceDto) {
    Microservice microservice = OrikaUtil.convert(microserviceDto, Microservice.class);
    microservice.setServiceId(uniqueIdService.getUniqueId());
    microservice.setOwner("admin");
    microservice.setCreateTime(System.currentTimeMillis());
    microservice.setUpdateTime(System.currentTimeMillis());
    boolean save = save(microservice);
    return save ? microservice.getServiceId() : null;
  }

  public String updateService(MicroserviceDTO microserviceDto) {
    Microservice microservice = OrikaUtil.convert(microserviceDto, Microservice.class);
    microservice.setUpdateTime(System.currentTimeMillis());
    boolean update = update(microservice, Wrappers.lambdaUpdate(Microservice.class)
        .eq(Microservice::getServiceId, microservice.getServiceId()));
    return update ? microserviceDto.getServiceId() : null;
  }

  public Boolean deleteService(String serviceId) {
    return remove(
        Wrappers.lambdaUpdate(Microservice.class).eq(Microservice::getServiceId, serviceId));
  }

  public MicroserviceDTO queryServiceDetail(String serviceId) {
    Microservice microservice = getOne(
        Wrappers.lambdaQuery(Microservice.class).eq(Microservice::getServiceId, serviceId));
    return OrikaUtil.convert(microservice, MicroserviceDTO.class);
  }

  public List<MicroserviceDTO> getServices() {
    return list().stream()
        .map(microservice -> OrikaUtil.convert(microservice, MicroserviceDTO.class))
        .collect(Collectors.toList());
  }

  public Microservice getServiceDetail(String serviceId) {
    return getOne(
        Wrappers.lambdaQuery(Microservice.class).eq(Microservice::getServiceId, serviceId));
  }
}
