package com.zj.domain.repository.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.service.ApiParamModel;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.service.ServiceApiBO;
import com.zj.domain.entity.po.service.ServiceApi;
import com.zj.domain.mapper.service.IServiceApiMapper;
import com.zj.domain.repository.service.IServiceApiRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author falcon
 * @since 2023/8/8
 */
@Repository
public class ServiceApiRepository extends ServiceImpl<IServiceApiMapper, ServiceApi> implements
        IServiceApiRepository {

    @Override
    public boolean saveApi(ServiceApiBO serviceApi) {
        ServiceApi api = convertServiceApi(serviceApi);
        api.setCreateTime(System.currentTimeMillis());
        api.setUpdateTime(System.currentTimeMillis());
        return save(api);
    }

    @Override
    @Transactional
    public boolean saveBatch(List<ServiceApiBO> serviceApis) {
        List<ServiceApi> serviceApiList = OrikaUtil.convertList(serviceApis, ServiceApi.class);
        return saveBatch(serviceApiList);
    }

    @Override
    public boolean updateApi(ServiceApiBO serviceApi) {
        ServiceApi api = convertServiceApi(serviceApi);
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
    public ServiceApiBO getServiceApi(String apiId) {
        ServiceApi api = getOne(Wrappers.lambdaUpdate(ServiceApi.class).eq(ServiceApi::getApiId, apiId));
        if (Objects.isNull(api)) {
            return null;
        }
        return convertServiceApiBO(api);
    }

    @Override
    public List<ServiceApiBO> getApiByService(String serviceId) {
        List<ServiceApi> serviceApis = list(Wrappers.lambdaQuery(ServiceApi.class).eq(ServiceApi::getServiceId, serviceId));
        if (CollectionUtils.isEmpty(serviceApis)) {
            return Collections.emptyList();
        }
        return serviceApis.stream().map(ServiceApiRepository::convertServiceApiBO).collect(Collectors.toList());
    }

    @Override
    public List<ServiceApiBO> getServiceApiList(List<String> apiIds) {
        List<ServiceApi> serviceApis = list(Wrappers.lambdaQuery(ServiceApi.class).in(ServiceApi::getApiId, apiIds));
        return serviceApis.stream().map(ServiceApiRepository::convertServiceApiBO).collect(Collectors.toList());
    }

    private static ServiceApiBO convertServiceApiBO(ServiceApi serviceApi) {
        ServiceApiBO serviceApiBO = OrikaUtil.convert(serviceApi, ServiceApiBO.class);
        serviceApiBO.setRequestParams(JSON.parseArray(serviceApi.getRequestParameter(), ApiParamModel.class));
        serviceApiBO.setResponseParams(JSON.parseArray(serviceApi.getResponseParameter(), ApiParamModel.class));
        return serviceApiBO;
    }

    private static ServiceApi convertServiceApi(ServiceApiBO serviceApiBO) {
        ServiceApi serviceApi = OrikaUtil.convert(serviceApiBO, ServiceApi.class);
        serviceApi.setRequestParameter(JSON.toJSONString(serviceApiBO.getRequestParams()));
        serviceApi.setResponseParameter(JSON.toJSONString(serviceApiBO.getResponseParams()));
        return serviceApi;
    }
}
