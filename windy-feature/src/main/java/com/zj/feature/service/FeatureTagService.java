package com.zj.feature.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.feature.entity.po.FeatureTag;
import com.zj.feature.mapper.FeatureTagMapper;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author falcon
 * @since 2023/1/28
 */
@Service
public class FeatureTagService extends ServiceImpl<FeatureTagMapper, FeatureTag> {

  public void batchAddTag(String featureId, List<String> tags) {
    long currentTime = System.currentTimeMillis();
    List<FeatureTag> tagList = tags.stream().distinct().map(tag -> {
      FeatureTag featureTag = new FeatureTag();
      featureTag.setFeatureId(featureId);
      featureTag.setTagValue(tag);
      featureTag.setCreateTime(currentTime);
      return featureTag;
    }).collect(Collectors.toList());

    saveBatch(tagList);
  }

  public void batchUpdateTag(String featureId, List<String> tags) {
    remove(Wrappers.lambdaQuery(FeatureTag.class).eq(FeatureTag::getFeatureId, featureId));
    if (!CollectionUtils.isEmpty(tags)) {
      batchAddTag(featureId, tags);
    }
  }

  public List<String> getFeatureTags(String featureId) {
    List<FeatureTag> featureTags = list(
        Wrappers.lambdaQuery(FeatureTag.class).eq(FeatureTag::getFeatureId, featureId));
    if (CollectionUtils.isEmpty(featureTags)) {
      return Collections.emptyList();
    }

    return featureTags.stream().map(FeatureTag::getTagValue).collect(Collectors.toList());
  }

  public List<String> getFeaturesByTag(List<String> tags) {
    List<FeatureTag> featureTags = list(
        Wrappers.lambdaQuery(FeatureTag.class).in(FeatureTag::getTagValue, tags));
    if (CollectionUtils.isEmpty(featureTags)) {
      return Collections.emptyList();
    }

    return featureTags.stream().map(FeatureTag::getFeatureId).collect(Collectors.toList());
  }
}
