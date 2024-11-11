package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.feature.FeatureInfoBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface IFeatureRepository {

  List<FeatureInfoBO> queryFeatureList(String testCaseId);

  List<FeatureInfoBO> queryNotContainFolder(String testCaseId);

  FeatureInfoBO getFeatureById(String featureId);

  boolean createFeature(FeatureInfoBO featureInfoBO);

  boolean updateFeatureInfo(FeatureInfoBO featureInfoBO);

  boolean deleteByFeatureId(String featureId);

  List<FeatureInfoBO> queryFeatureList(List<String> featureIds);

  Boolean batchDeleteByFeatureId(List<String> featureIds);

  IPage<FeatureInfoBO> queryFeaturePage(String testCaseId, int page, int size);

  boolean saveBatch(List<FeatureInfoBO> infoList);

  List<FeatureInfoBO> getSubFeatures(String featureId);

  Boolean batchUpdate(List<FeatureInfoBO> features);

    List<FeatureInfoBO> getCaseFeatures(String testCaseId);
  List<FeatureInfoBO> getFeatureByCases(List<String> testCaseIds);
}
