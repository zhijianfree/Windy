package com.zj.domain.repository.feature;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.po.feature.ExecutePoint;
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

  Page<ExecutePointDto> queryExecutePointPage(String featureId, int page, int size);

  void saveBatch(List<ExecutePointDto> newExecutePoints);
}
