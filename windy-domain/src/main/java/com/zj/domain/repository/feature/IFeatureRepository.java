package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface IFeatureRepository {

  List<FeatureInfoDto> queryFeatureList(String testCaseId);

  List<FeatureInfoDto> queryNotContainFolder(String testCaseId);

  FeatureInfoDto getFeatureById(String featureId);

  boolean createFeature(FeatureInfoDto featureInfoDto);

  boolean updateFeatureInfo(FeatureInfoDto featureInfoDto);

  boolean deleteByFeatureId(String featureId);

  List<FeatureInfoDto> queryFeatureList(List<String> featureIds);

  Boolean batchDeleteByFeatureId(List<String> featureIds);

  IPage<FeatureInfoDto> queryFeaturePage(String testCaseId, int page, int size);

  boolean saveBatch(List<FeatureInfoDto> infoList);

  List<FeatureInfoDto> getSubFeatures(String featureId);

  Boolean batchUpdate(List<FeatureInfoDto> features);
}
