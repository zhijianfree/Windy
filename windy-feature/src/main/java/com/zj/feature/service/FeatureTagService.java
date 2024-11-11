package com.zj.feature.service;

import com.zj.domain.entity.bo.feature.FeatureTagBO;
import com.zj.domain.repository.feature.IFeatureTagRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2023/1/28
 */
@Service
public class FeatureTagService{

  private final IFeatureTagRepository featureTagRepository;

  public FeatureTagService(IFeatureTagRepository featureTagRepository) {
    this.featureTagRepository = featureTagRepository;
  }

  public void batchAddTag(String featureId, List<String> tags) {
    long currentTime = System.currentTimeMillis();
    List<FeatureTagBO> tagList = tags.stream().distinct().map(tag -> {
      FeatureTagBO featureTag = new FeatureTagBO();
      featureTag.setFeatureId(featureId);
      featureTag.setTagValue(tag);
      featureTag.setCreateTime(currentTime);
      return featureTag;
    }).collect(Collectors.toList());

    featureTagRepository.saveBatchTag(tagList);
  }

  public void batchUpdateTag(String featureId, List<String> tags) {
    featureTagRepository.deleteByFeatureId(featureId);
    if (!CollectionUtils.isEmpty(tags)) {
      batchAddTag(featureId, tags);
    }
  }

  public List<String> getFeatureTags(String featureId) {
    List<FeatureTagBO> featureTags = featureTagRepository.getFeatureTags(featureId);
    if (CollectionUtils.isEmpty(featureTags)) {
      return Collections.emptyList();
    }

    return featureTags.stream().map(FeatureTagBO::getTagValue).collect(Collectors.toList());
  }

  public List<String> getFeaturesByTag(List<String> tags) {
    List<FeatureTagBO> featureTags = featureTagRepository.getFeaturesByTag(tags);
    if (CollectionUtils.isEmpty(featureTags)) {
      return Collections.emptyList();
    }

    return featureTags.stream().map(FeatureTagBO::getFeatureId).collect(Collectors.toList());
  }
}
