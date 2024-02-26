package com.zj.domain.repository.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.service.ServiceApiDto;
import com.zj.domain.entity.po.service.ServiceApi;
import com.zj.domain.mapper.service.IServiceApiMapper;
import com.zj.domain.repository.service.IServiceApiRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

/**
 * @author falcon
 * @since 2023/8/8
 */
@Repository
public class ServiceApiRepository extends ServiceImpl<IServiceApiMapper, ServiceApi> implements
    IServiceApiRepository {

  @Override
  public boolean saveApi(ServiceApiDto serviceApi) {
    ServiceApi api = OrikaUtil.convert(serviceApi, ServiceApi.class);
    api.setCreateTime(System.currentTimeMillis());
    api.setUpdateTime(System.currentTimeMillis());
    return save(api);
  }

  @Override
  public boolean updateApi(ServiceApiDto serviceApi) {
    ServiceApi api = OrikaUtil.convert(serviceApi, ServiceApi.class);
    api.setUpdateTime(System.currentTimeMillis());
    return update(api,
        Wrappers.lambdaUpdate(ServiceApi.class).eq(ServiceApi::getApiId, serviceApi.getApiId()));
  }

  @Override
  public boolean deleteApi(String apiId) {
    return remove(Wrappers.lambdaUpdate(ServiceApi.class).eq(ServiceApi::getApiId, apiId));
  }

  @Override
  public boolean batchDeleteApi(List<String> apiIds) {
    if (CollectionUtils.isEmpty(apiIds)) {
      return false;
    }
    return remove(Wrappers.lambdaUpdate(ServiceApi.class).in(ServiceApi::getApiId, apiIds));
  }

  @Override
  public ServiceApiDto getServiceApi(String apiId) {
    ServiceApi api = getOne(
        Wrappers.lambdaUpdate(ServiceApi.class).eq(ServiceApi::getApiId, apiId));
    if (Objects.isNull(api)) {
      return null;
    }
    return OrikaUtil.convert(api, ServiceApiDto.class);
  }

  @Override
  public List<ServiceApiDto> getApiByService(String serviceId) {
    List<ServiceApi> list = list(
        Wrappers.lambdaQuery(ServiceApi.class).eq(ServiceApi::getServiceId, serviceId));
    if (CollectionUtils.isEmpty(list)) {
      return Collections.emptyList();
    }
    return OrikaUtil.convertList(list, ServiceApiDto.class);
  }

  @Override
  public List<ServiceApiDto> getServiceApiList(List<String> apiIds) {
    List<ServiceApi> serviceApis = list(Wrappers.lambdaQuery(ServiceApi.class).in(ServiceApi::getApiId, apiIds));
    return OrikaUtil.convertList(serviceApis, ServiceApiDto.class);
  }
}
