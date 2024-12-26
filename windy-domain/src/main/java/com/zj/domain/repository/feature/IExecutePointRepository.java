package com.zj.domain.repository.feature;

import com.zj.domain.entity.bo.feature.ExecutePointBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
public interface IExecutePointRepository {

  /**
   * 获取执行点
   * @param executePointId 执行点ID
   * @return 执行点
   */
  ExecutePointBO getExecutePoint(String executePointId);

  /**
   * 获取用例下的执行点
   * @param featureId 特性ID
   * @return 执行点列表
   */
  List<ExecutePointBO> getExecutePointByFeatureId(String featureId);

  /**
   * 更新执行点
   * @param executePoint 执行点
   * @return 是否成功
   */
  boolean updateExecutePoint(ExecutePointBO executePoint);

  /**
   * 删除执行点
   * @param pointId 执行点ID
   * @return 是否成功
   */
  boolean deleteExecutePoint(String pointId);

  /**
   * 删除用例下的执行点
   * @param featureId 特性ID
   * @return 是否成功
   */
  boolean deleteByFeatureId(String featureId);

  /**
   * 根据用例ID列表删除执行点
   * @param featureIds 用例ID列表
   * @return 是否成功
   */
  boolean deleteByFeatureIds(List<String> featureIds);

  /**
   * 根据用例ID列表获取执行点
   * @param featureIds 用例ID列表
   * @return 执行点列表
   */
  List<ExecutePointBO> getPointsByFeatureIds(List<String> featureIds);

  /**
   * 保存执行点
   * @param executePoint 执行点
   * @return 是否成功
   */
  boolean saveExecutePoint(ExecutePointBO executePoint);

  /**
   * 批量保存执行点
   * @param newExecutePoints 执行点列表
   * @return 是否成功
   */
  boolean batchSavePoints(List<ExecutePointBO> newExecutePoints);

  /**
   * 批量更新执行点
   * @param newExecutePoints 执行点列表
   * @return 是否成功
   */
  boolean updateBatch(List<ExecutePointBO> newExecutePoints);

  /**
   * 获取模板下的执行点
   * @param templateId 模板ID
   * @return 执行点列表
   */
  List<ExecutePointBO> getTemplateExecutePoints(String templateId);
}
