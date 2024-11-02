package com.zj.domain.repository.feature.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.feature.CompareDefine;
import com.zj.common.entity.feature.ExecutorUnit;
import com.zj.common.entity.feature.VariableDefine;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.feature.ExecutePointBO;
import com.zj.domain.entity.po.feature.ExecutePoint;
import com.zj.domain.mapper.feeature.ExecutePointMapper;
import com.zj.domain.repository.feature.IExecutePointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Repository
public class ExecutePointRepository extends ServiceImpl<ExecutePointMapper, ExecutePoint> implements
    IExecutePointRepository {

  @Override
  public ExecutePointBO getExecutePoint(String executePointId) {
    ExecutePoint executePoint = getOne(
        Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getPointId, executePointId));
    return convertExecutePointBO(executePoint);
  }

  @Override
  public List<ExecutePointBO> getExecutePointByFeatureId(String featureId) {
    List<ExecutePoint> pointList = list(
        Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getFeatureId, featureId)
            .orderByDesc(ExecutePoint::getCreateTime));
    return convertExecutePointBOList(pointList);
  }

  @Override
  public boolean updateExecutePoint(ExecutePointBO executePointBO) {
    ExecutePoint executePoint = convertExecutePoint(executePointBO);
    executePoint.setUpdateTime(System.currentTimeMillis());
    return update(executePoint, Wrappers.lambdaUpdate(ExecutePoint.class)
        .eq(ExecutePoint::getPointId, executePoint.getPointId()));
  }

  @Override
  public boolean deleteExecutePoint(String pointId) {
    return remove(Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getPointId, pointId));
  }

  @Override
  public boolean deleteByFeatureId(String featureId) {
    return remove(
        Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getFeatureId, featureId));
  }

  @Override
  public boolean deleteByFeatureIds(List<String> featureIds) {
    return remove(
            Wrappers.lambdaQuery(ExecutePoint.class).in(ExecutePoint::getFeatureId, featureIds));
  }

  @Override
  public List<ExecutePointBO> getPointsByFeatureIds(List<String> featureIds) {
    List<ExecutePoint> pointList = list(
        Wrappers.lambdaQuery(ExecutePoint.class).in(ExecutePoint::getFeatureId, featureIds)
            .orderByDesc(ExecutePoint::getCreateTime));
    return convertExecutePointBOList(pointList);
  }

  @Override
  public boolean saveExecutePoint(ExecutePointBO executePointBO) {
    ExecutePoint executePoint = convertExecutePoint(executePointBO);
    long dateNow = System.currentTimeMillis();
    executePoint.setCreateTime(dateNow);
    executePoint.setUpdateTime(dateNow);
    return save(executePoint);
  }

  @Override
  public boolean saveBatch(List<ExecutePointBO> executePoints) {
    List<ExecutePoint> pointList = convertExecutePointList(executePoints);
    return saveBatch(pointList);
  }


  @Override
  @Transactional
  public boolean updateBatch(List<ExecutePointBO> executePointBOList) {
    List<ExecutePoint> pointList = convertExecutePointList(executePointBOList);
    return updateBatchById(pointList);
  }

  @Override
  public List<ExecutePointBO> getTemplateExecutePoints(String templateId) {
    List<ExecutePoint> executePoints = list(
        Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getTemplateId, templateId));
    return convertExecutePointBOList(executePoints);
  }

  public List<ExecutePointBO> convertExecutePointBOList(List<ExecutePoint> executePoints){
    return executePoints.stream().map(this::convertExecutePointBO).collect(Collectors.toList());
  }
  public ExecutePointBO convertExecutePointBO(ExecutePoint executePoint){
    ExecutePointBO executePointBO = OrikaUtil.convert(executePoint, ExecutePointBO.class);
    executePointBO.setCompareDefine(JSON.parseArray(executePoint.getCompareDefine(), CompareDefine.class));
    executePointBO.setFeatureInfo(JSON.parseObject(executePoint.getFeatureInfo(), ExecutorUnit.class));
    executePointBO.setVariables(JSON.parseArray(executePoint.getVariables(), VariableDefine.class));
    return executePointBO;
  }

  public ExecutePoint convertExecutePoint(ExecutePointBO executePointBO){
    ExecutePoint executePoint = OrikaUtil.convert(executePointBO, ExecutePoint.class);
    executePoint.setCompareDefine(JSON.toJSONString(executePointBO.getCompareDefine()));
    executePoint.setFeatureInfo(JSON.toJSONString(executePointBO.getFeatureInfo()));
    executePoint.setVariables(JSON.toJSONString(executePointBO.getVariables()));
    return executePoint;
  }

  private List<ExecutePoint> convertExecutePointList(List<ExecutePointBO> executePoints) {
    return executePoints.stream().map(this::convertExecutePoint).collect(Collectors.toList());
  }
}
