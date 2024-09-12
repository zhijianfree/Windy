package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.PublishBindDto;
import com.zj.domain.entity.po.pipeline.PublishBind;
import com.zj.domain.mapper.pipeline.PublishBindMapper;
import com.zj.domain.repository.pipeline.IPublishBindRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
@Repository
public class PublishBindRepository extends ServiceImpl<PublishBindMapper, PublishBind> implements
    IPublishBindRepository {

  @Override
  public boolean createPublish(PublishBindDto publishBind) {
    PublishBind publish = OrikaUtil.convert(publishBind, PublishBind.class);
    long dateNow = System.currentTimeMillis();
    publish.setCreateTime(dateNow);
    publish.setUpdateTime(dateNow);
    return save(publish);
  }

  @Override
  public boolean updatePublish(PublishBindDto publishBind) {
    PublishBind publish = OrikaUtil.convert(publishBind, PublishBind.class);
    publish.setUpdateTime(System.currentTimeMillis());
    return update(publish, Wrappers.lambdaUpdate(PublishBind.class)
        .eq(PublishBind::getPublishId, publish.getPublishId()));
  }

  @Override
  public boolean deletePublish(String publishId) {
    return remove(Wrappers.lambdaQuery(PublishBind.class).eq(PublishBind::getPublishId, publishId));
  }

  @Override
  public List<PublishBindDto> getServicePublishes(String serviceId) {
    List<PublishBind> publishBinds = list(
        Wrappers.lambdaQuery(PublishBind.class).eq(PublishBind::getServiceId, serviceId));
    return OrikaUtil.convertList(publishBinds, PublishBindDto.class);
  }

  @Override
  public PublishBindDto getServiceBranch(String serviceId, String gitBranch) {
    PublishBind publishBind = getOne(
        Wrappers.lambdaQuery(PublishBind.class).eq(PublishBind::getServiceId, serviceId)
            .eq(PublishBind::getBranch, gitBranch));
    if (Objects.isNull(publishBind)) {
      return null;
    }
    return OrikaUtil.convert(publishBind, PublishBindDto.class);
  }

  @Override
  public boolean deletePipelinePublishes(String masterLineId) {
    return remove( Wrappers.lambdaQuery(PublishBind.class).eq(PublishBind::getPublishLine, masterLineId));
  }

  @Override
  public List<PublishBindDto> getPipelinePublishes(String pipelineId) {
    List<PublishBind> publishBinds = list(
            Wrappers.lambdaQuery(PublishBind.class).eq(PublishBind::getPipelineId, pipelineId));
    return OrikaUtil.convertList(publishBinds, PublishBindDto.class);
  }
}
