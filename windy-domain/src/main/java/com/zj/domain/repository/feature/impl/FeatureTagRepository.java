package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.FeatureTagBO;
import com.zj.domain.entity.po.feature.FeatureTag;
import com.zj.domain.mapper.feeature.FeatureTagMapper;
import com.zj.domain.repository.feature.IFeatureTagRepository;

import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Repository
public class FeatureTagRepository extends ServiceImpl<FeatureTagMapper, FeatureTag> implements
    IFeatureTagRepository {

  @Override
  @Transactional
  public boolean saveBatchTag(List<FeatureTagBO> tagList) {
    if (CollectionUtils.isEmpty(tagList)) {
      return false;
    }
    List<FeatureTag> featureTags = OrikaUtil.convertList(tagList, FeatureTag.class);
    return saveBatch(featureTags);
  }

  @Override
  public boolean deleteByFeatureId(String featureId) {
    return remove(Wrappers.lambdaQuery(FeatureTag.class).eq(FeatureTag::getFeatureId, featureId));
  }

  @Override
  public List<FeatureTagBO> getFeatureTags(String featureId) {
    List<FeatureTag> featureTags = list(
        Wrappers.lambdaQuery(FeatureTag.class).eq(FeatureTag::getFeatureId, featureId));
    return OrikaUtil.convertList(featureTags, FeatureTagBO.class);
  }

  @Override
  public List<FeatureTagBO> getFeaturesByTag(List<String> tags) {
    if (CollectionUtils.isEmpty(tags)) {
      return Collections.emptyList();
    }
    List<FeatureTag> featureTags = list(
        Wrappers.lambdaQuery(FeatureTag.class).in(FeatureTag::getTagValue, tags));
    return OrikaUtil.convertList(featureTags, FeatureTagBO.class);
  }
}
