package com.zj.feature.service;

import com.zj.domain.entity.bo.feature.FeatureTagBO;
import com.zj.domain.repository.feature.IFeatureTagRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author guyuelan
 * @since 2023/1/28
 */
@Slf4j
@Service
public class FeatureTagService{

  private final IFeatureTagRepository featureTagRepository;

  public FeatureTagService(IFeatureTagRepository featureTagRepository) {
    this.featureTagRepository = featureTagRepository;
  }

  public boolean batchAddTag(String featureId, List<String> tags) {
    long currentTime = System.currentTimeMillis();
    List<FeatureTagBO> tagList = tags.stream().distinct().map(tag -> {
      FeatureTagBO featureTag = new FeatureTagBO();
      featureTag.setFeatureId(featureId);
      featureTag.setTagValue(tag);
      featureTag.setCreateTime(currentTime);
      return featureTag;
    }).collect(Collectors.toList());

    return featureTagRepository.saveBatchTag(tagList);
  }

  @Transactional
  public boolean batchUpdateTag(String featureId, List<String> tags) {
    if (CollectionUtils.isEmpty(tags)) {
      log.info("tag is empty, update fail");
      return false;
    }
    boolean deleteByFeatureId = featureTagRepository.deleteByFeatureId(featureId);
    log.info("delete feature tag result={}", deleteByFeatureId);
    return batchAddTag(featureId, tags);
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
