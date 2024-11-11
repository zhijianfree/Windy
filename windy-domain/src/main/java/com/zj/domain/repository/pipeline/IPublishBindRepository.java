package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PublishBindBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
public interface IPublishBindRepository {

  boolean createPublish(PublishBindBO publishBind);

  boolean updatePublish(PublishBindBO publishBind);

  boolean deletePublish(String publishId);

  List<PublishBindBO> getServicePublishes(String serviceId);

  PublishBindBO getServiceBranch(String serviceId, String gitBranch);

  boolean deleteServicePublishes(String serviceId);

    PublishBindBO getPublishById(String publishId);
}
