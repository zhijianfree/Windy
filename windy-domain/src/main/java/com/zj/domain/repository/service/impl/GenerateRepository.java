package com.zj.domain.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.service.ServiceGenerateBO;
import com.zj.domain.entity.po.service.ServiceGenerate;
import com.zj.domain.mapper.service.ServiceGenerateMapper;
import com.zj.domain.repository.service.IGenerateRepository;
import org.springframework.stereotype.Repository;

@Repository
public class GenerateRepository extends
    ServiceImpl<ServiceGenerateMapper, ServiceGenerate> implements IGenerateRepository {

  @Override
  public ServiceGenerateBO getByService(String serviceId) {
    ServiceGenerate serviceGenerate = getOne(
        Wrappers.lambdaQuery(ServiceGenerate.class).eq(ServiceGenerate::getServiceId, serviceId));
    return OrikaUtil.convert(serviceGenerate, ServiceGenerateBO.class);
  }

  @Override
  public boolean create(ServiceGenerateBO serviceGenerateBO) {
    ServiceGenerate serviceGenerate = OrikaUtil.convert(serviceGenerateBO, ServiceGenerate.class);
    serviceGenerate.setCreateTime(System.currentTimeMillis());
    serviceGenerate.setUpdateTime(System.currentTimeMillis());
    return save(serviceGenerate);
  }

  @Override
  public boolean update(ServiceGenerateBO serviceGenerateBO) {
    ServiceGenerate serviceGenerate = OrikaUtil.convert(serviceGenerateBO, ServiceGenerate.class);
    serviceGenerate.setUpdateTime(System.currentTimeMillis());
    return update(serviceGenerate, Wrappers.lambdaUpdate(ServiceGenerate.class)
        .eq(ServiceGenerate::getGenerateId, serviceGenerateBO.getGenerateId()));
  }
}
