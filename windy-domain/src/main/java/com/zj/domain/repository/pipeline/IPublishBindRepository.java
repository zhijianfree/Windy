package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PublishBindBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/28
 */
public interface IPublishBindRepository {

  /**
   * 创建流水线发布
   * @param publishBind 发布信息
   * @return 是否成功
   */
  boolean createPublish(PublishBindBO publishBind);

  /**
   * 更新流水线发布
   * @param publishBind 发布信息
   * @return 是否成功
   */
  boolean updatePublish(PublishBindBO publishBind);

  /**
   * 删除流水线发布
   * @param publishId 发布ID
   * @return 是否成功
   */
  boolean deletePublish(String publishId);

  /**
   * 获取服务发布列表
   * @param serviceId 服务ID
   * @return 发布列表
   */
  List<PublishBindBO> getServicePublishes(String serviceId);

  /**
   * 获取服务指定分支的发布信息
   * @param serviceId 服务ID
   * @param gitBranch 分支
   * @return 发布信息
   */
  PublishBindBO getServiceBranch(String serviceId, String gitBranch);

  /**
   * 删除服务发布信息
   * @param serviceId 服务ID
   * @return 是否成功
   */
  boolean deleteServicePublishes(String serviceId);

  /**
   * 获取发布信息
   * @param publishId 发布ID
   * @return 发布信息
   */
  PublishBindBO getPublishById(String publishId);
}
