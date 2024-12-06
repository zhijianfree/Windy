package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.FeatureTagBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
public interface IFeatureTagRepository {

  boolean saveBatchTag(List<FeatureTagBO> tagList);

  boolean deleteByFeatureId(String featureId);

  List<FeatureTagBO> getFeatureTags(String featureId);

  List<FeatureTagBO> getFeaturesByTag(List<String> tags);
}
