package com.zj.domain.repository.feature;

import com.zj.domain.entity.dto.feature.FeatureTagDto;
import com.zj.domain.entity.po.feature.FeatureTag;
import java.util.List;

/**
 * @author falcon
 * @since 2023/5/18
 */
public interface IFeatureTagRepository {

  void saveBatchTag(List<FeatureTagDto> tagList);

  boolean deleteByFeatureId(String featureId);

  List<FeatureTagDto> getFeatureTags(String featureId);

  List<FeatureTagDto> getFeaturesByTag(List<String> tags);
}
