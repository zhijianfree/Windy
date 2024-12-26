package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.FeatureTagBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface IFeatureTagRepository {

  /**
   * 批量保存用例标签
   * @param tagList 标签列表
   * @return 是否成功
   */
  boolean saveBatchTag(List<FeatureTagBO> tagList);

  /**
   * 删除用例标签
   * @param featureId 用例ID
   * @return 是否成功
   */
  boolean deleteByFeatureId(String featureId);

  /**
   * 获取用例标签
   * @param featureId 用例ID
   * @return 标签列表
   */
  List<FeatureTagBO> getFeatureTags(String featureId);

  /**
   * 根据标签获取用例
   * @param tags 标签列表
   * @return 用例列表
   */
  List<FeatureTagBO> getFeaturesByTag(List<String> tags);
}
