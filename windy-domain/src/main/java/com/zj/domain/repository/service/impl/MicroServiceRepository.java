package com.zj.domain.repository.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.auth.UserDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.entity.po.service.Microservice;
import com.zj.domain.entity.po.service.ServiceMember;
import com.zj.domain.mapper.service.MicroServiceMapper;
import com.zj.domain.mapper.service.ServiceMemberMapper;
import com.zj.domain.repository.auth.IUserRepository;
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

  private final ServiceMemberMapper serviceMemberMapper;

  private final IUserRepository userRepository;

  public MicroServiceRepository(ServiceMemberMapper serviceMemberMapper, IUserRepository userRepository) {
    this.serviceMemberMapper = serviceMemberMapper;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional
  public String createService(String userId, MicroserviceDto microserviceDto) {
    Microservice microservice = OrikaUtil.convert(microserviceDto, Microservice.class);
    microservice.setCreateTime(System.currentTimeMillis());
    microservice.setUpdateTime(System.currentTimeMillis());
    boolean save = save(microservice);
    if (save) {
      boolean result = addServiceMember(microservice.getServiceId(), userId);
      log.info("add service member result = {}", result);
    }
    return save ? microservice.getServiceId() : null;
  }

  @Override
  public String updateService(MicroserviceDto microserviceDto) {
    Microservice microservice = OrikaUtil.convert(microserviceDto, Microservice.class);
    microservice.setUpdateTime(System.currentTimeMillis());
    boolean update = update(microservice, Wrappers.lambdaUpdate(Microservice.class)
        .eq(Microservice::getServiceId, microservice.getServiceId()));
    return update ? microserviceDto.getServiceId() : null;
  }

  @Override
  public Boolean deleteService(String serviceId) {
    return remove(
        Wrappers.lambdaUpdate(Microservice.class).eq(Microservice::getServiceId, serviceId));
  }

  @Override
  public MicroserviceDto queryServiceDetail(String serviceId) {
    Microservice microservice = getOne(
        Wrappers.lambdaQuery(Microservice.class).eq(Microservice::getServiceId, serviceId));
    return OrikaUtil.convert(microservice, MicroserviceDto.class);
  }

  @Override
  public List<MicroserviceDto> getServices(String currentUserId) {
    List<ServiceMember> serviceMembers = serviceMemberMapper.selectList(Wrappers.lambdaQuery(ServiceMember.class).eq(ServiceMember::getUserId,
            currentUserId));
    if (CollectionUtils.isEmpty(serviceMembers)) {
      return Collections.emptyList();
    }
    List<String> serviceIds = serviceMembers.stream().map(ServiceMember::getServiceId).collect(Collectors.toList());
    return list(Wrappers.lambdaQuery(Microservice.class).in(Microservice::getServiceId, serviceIds)).stream()
        .map(microservice -> OrikaUtil.convert(microservice, MicroserviceDto.class))
        .collect(Collectors.toList());
  }

  @Override
  public IPage<MicroserviceDto> getServices(Integer pageNo, Integer size, String name) {
    IPage<Microservice> iPage = new Page<>(pageNo, size);
    LambdaQueryWrapper<Microservice> queryWrapper = Wrappers.lambdaQuery(Microservice.class);
    if (!StringUtils.isEmpty(name)) {
      queryWrapper.like(Microservice::getServiceName, name);
    }
    IPage<Microservice> pageList = page(iPage, queryWrapper);

    IPage<MicroserviceDto> page = new Page<>();
    page.setTotal(pageList.getTotal());
    page.setRecords(OrikaUtil.convertList(pageList.getRecords(), MicroserviceDto.class));
    return page;
  }

  @Override
  public MicroserviceDto queryServiceByName(String serviceName) {
    Microservice microservice = getOne(
        Wrappers.lambdaQuery(Microservice.class).eq(Microservice::getServiceName, serviceName));
    return OrikaUtil.convert(microservice, MicroserviceDto.class);
  }

  @Override
  public boolean addServiceMember(String serviceId, String userId) {
    ServiceMember serviceMember = new ServiceMember();
    serviceMember.setServiceId(serviceId);
    serviceMember.setUserId(userId);
    serviceMember.setCreateTime(System.currentTimeMillis());
    return serviceMemberMapper.insert(serviceMember) > 0;
  }

  @Override
  public List<UserDto> getServiceMembers(String serviceId) {
    List<ServiceMember> serviceMembers = serviceMemberMapper.selectList(Wrappers.lambdaQuery(ServiceMember.class).eq(ServiceMember::getServiceId,
            serviceId));
    if (CollectionUtils.isEmpty(serviceMembers)) {
      return Collections.emptyList();
    }
    List<String> userIds = serviceMembers.stream().map(ServiceMember::getUserId).collect(Collectors.toList());
    return userRepository.getUserByUserList(userIds);
  }

  @Override
  public Boolean deleteServiceMember(String serviceId, String userId) {
    return serviceMemberMapper.delete(Wrappers.lambdaQuery(ServiceMember.class).eq(ServiceMember::getServiceId,
            serviceId).eq(ServiceMember::getUserId, userId)) > 0;
  }
}
