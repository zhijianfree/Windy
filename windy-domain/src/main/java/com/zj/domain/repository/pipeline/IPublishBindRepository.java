package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.dto.pipeline.PublishBindDto;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
public interface IPublishBindRepository {

  boolean createPublish(PublishBindDto publishBind);

  boolean updatePublish(PublishBindDto publishBind);

  boolean deletePublish(String publishId);

  List<PublishBindDto> getServicePublishes(String serviceId);

  PublishBindDto getServiceBranch(String serviceId, String gitBranch);

  boolean deleteServicePublishes(String serviceId);

    PublishBindDto getPublishById(String publishId);
}
