package com.zj.domain.repository.feature;

import com.zj.domain.entity.dto.feature.ExecutePointDto;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface IExecutePointRepository {

  ExecutePointDto getExecutePoint(String executePointId);

  List<ExecutePointDto> getExecutePointByFeatureId(String featureId);

  List<ExecutePointDto> getExecutePointByFeatureIds(List<String> featureIds);

  boolean updateExecutePoint(ExecutePointDto executePoint);

  boolean deleteExecutePoint(String pointId);

  boolean deleteByFeatureId(String featureId);

  List<ExecutePointDto> getPointsByFeatureIds(List<String> featureIds);

  boolean saveExecutePoint(ExecutePointDto executePoint);

  boolean saveBatch(List<ExecutePointDto> newExecutePoints);

  boolean updateBatch(List<ExecutePointDto> newExecutePoints);

  List<ExecutePointDto> getTemplateExecutePoints(String templateId);
}
