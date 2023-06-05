package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.FeatureTagDto;
import com.zj.domain.entity.po.feature.FeatureTag;
import com.zj.domain.mapper.feeature.FeatureTagMapper;
import com.zj.domain.repository.feature.IFeatureTagRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/18
 */
@Repository
public class FeatureTagRepository extends ServiceImpl<FeatureTagMapper, FeatureTag> implements
    IFeatureTagRepository {

  @Override
  public void saveBatchTag(List<FeatureTagDto> tagList) {
    List<FeatureTag> featureTags = OrikaUtil.convertList(tagList, FeatureTag.class);
    saveBatch(featureTags);
  }

  @Override
  public boolean deleteByFeatureId(String featureId) {
    return remove(Wrappers.lambdaQuery(FeatureTag.class).eq(FeatureTag::getFeatureId, featureId));
  }

  @Override
  public List<FeatureTagDto> getFeatureTags(String featureId) {
    List<FeatureTag> featureTags = list(
        Wrappers.lambdaQuery(FeatureTag.class).eq(FeatureTag::getFeatureId, featureId));
    return OrikaUtil.convertList(featureTags, FeatureTagDto.class);
  }

  @Override
  public List<FeatureTagDto> getFeaturesByTag(List<String> tags) {
    List<FeatureTag> featureTags = list(
        Wrappers.lambdaQuery(FeatureTag.class).in(FeatureTag::getTagValue, tags));
    return OrikaUtil.convertList(featureTags, FeatureTagDto.class);
  }
}
