package com.zj.domain.repository.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.pipeline.ServiceConfig;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.entity.bo.service.ResourceMemberDto;
import com.zj.domain.entity.po.service.Microservice;
import com.zj.domain.entity.po.service.ResourceMember;
import com.zj.domain.mapper.service.MicroServiceMapper;
import com.zj.domain.repository.demand.IMemberRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * @author guyuelan
 * @since 2023/6/20
 */

@Slf4j
@Repository
public class MicroServiceRepository extends ServiceImpl<MicroServiceMapper, Microservice> implements
    IMicroServiceRepository {

  private final IMemberRepository memberRepository;

  public MicroServiceRepository(IMemberRepository memberRepository) {
    this.memberRepository = memberRepository;
  }


  @Override
  @Transactional
  public String createService(String userId, MicroserviceBO microserviceBO) {
    Microservice microservice = convertService(microserviceBO);
    microservice.setCreateTime(System.currentTimeMillis());
    microservice.setUpdateTime(System.currentTimeMillis());
    boolean save = save(microservice);
    if (save) {
      ResourceMemberDto resourceMemberDto = new ResourceMemberDto();
      resourceMemberDto.setResourceId(microservice.getServiceId());
      resourceMemberDto.setUserId(userId);
      boolean result = memberRepository.addResourceMember(resourceMemberDto);
      log.info("add service member result = {}", result);
    }
    return save ? microservice.getServiceId() : null;
  }

  @Override
  public String updateService(MicroserviceBO microserviceBO) {
    Microservice microservice = convertService(microserviceBO);
    microservice.setUpdateTime(System.currentTimeMillis());
    boolean update = update(microservice, Wrappers.lambdaUpdate(Microservice.class)
        .eq(Microservice::getServiceId, microservice.getServiceId()));
    return update ? microserviceBO.getServiceId() : null;
  }

  @Override
  public Boolean deleteService(String serviceId) {
    return remove(
        Wrappers.lambdaUpdate(Microservice.class).eq(Microservice::getServiceId, serviceId));
  }

  @Override
  public MicroserviceBO queryServiceDetail(String serviceId) {
    Microservice microservice = getOne(
        Wrappers.lambdaQuery(Microservice.class).eq(Microservice::getServiceId, serviceId));
    return convertServiceBO(microservice);
  }

  @Override
  public List<MicroserviceBO> getServices(String currentUserId) {
    List<ResourceMember> resourceMembers = memberRepository.getResourceMembersByUser(currentUserId);
    if (CollectionUtils.isEmpty(resourceMembers)) {
      return Collections.emptyList();
    }
    List<String> serviceIds = resourceMembers.stream().map(ResourceMember::getResourceId).collect(Collectors.toList());
    return list(Wrappers.lambdaQuery(Microservice.class).in(Microservice::getServiceId, serviceIds)).stream()
        .map(MicroServiceRepository::convertServiceBO)
        .collect(Collectors.toList());
  }

  @Override
  public IPage<MicroserviceBO> getServices(Integer pageNo, Integer size, String name, List<String> serviceIds) {
    IPage<Microservice> iPage = new Page<>(pageNo, size);
    LambdaQueryWrapper<Microservice> queryWrapper = Wrappers.lambdaQuery(Microservice.class);
    if (!StringUtils.isEmpty(name)) {
      queryWrapper.like(Microservice::getServiceName, name);
    }

    if (CollectionUtils.isNotEmpty(serviceIds)) {
      queryWrapper.in(Microservice::getServiceId, serviceIds);
    }

    IPage<Microservice> pageList = page(iPage, queryWrapper);
    IPage<MicroserviceBO> page = new Page<>();
    page.setTotal(pageList.getTotal());
    page.setRecords(convertServiceBOList(pageList.getRecords()));
    return page;
  }

  @Override
  public MicroserviceBO queryServiceByName(String serviceName) {
    Microservice microservice = getOne(
        Wrappers.lambdaQuery(Microservice.class).eq(Microservice::getServiceName, serviceName));
    return convertServiceBO(microservice);
  }

  @Override
  public List<MicroserviceBO> getAllServices() {
    List<Microservice> list = list();
    return convertServiceBOList(list);
  }

  private static Microservice convertService(MicroserviceBO microserviceBO) {
    Microservice microservice = OrikaUtil.convert(microserviceBO, Microservice.class);
    microservice.setConfig(JSON.toJSONString(microserviceBO.getServiceConfig()));
    return microservice;
  }

  private static MicroserviceBO convertServiceBO(Microservice microservice) {
    MicroserviceBO microserviceBO = OrikaUtil.convert(microservice, MicroserviceBO.class);
    microserviceBO.setServiceConfig(JSON.parseObject(microservice.getConfig(), ServiceConfig.class));
    return microserviceBO;
  }

  private static List<MicroserviceBO> convertServiceBOList(List<Microservice> serviceList) {
    return serviceList.stream().map(MicroServiceRepository::convertServiceBO).collect(Collectors.toList());
  }
}
