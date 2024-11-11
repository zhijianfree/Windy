package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.ExecutePointBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface IExecutePointRepository {

  ExecutePointBO getExecutePoint(String executePointId);

  List<ExecutePointBO> getExecutePointByFeatureId(String featureId);

  boolean updateExecutePoint(ExecutePointBO executePoint);

  boolean deleteExecutePoint(String pointId);

  boolean deleteByFeatureId(String featureId);
  boolean deleteByFeatureIds(List<String> featureIds);

  List<ExecutePointBO> getPointsByFeatureIds(List<String> featureIds);

  boolean saveExecutePoint(ExecutePointBO executePoint);

  boolean saveBatch(List<ExecutePointBO> newExecutePoints);

  boolean updateBatch(List<ExecutePointBO> newExecutePoints);

  List<ExecutePointBO> getTemplateExecutePoints(String templateId);
}
