package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.po.feature.ExecutePoint;
import com.zj.domain.mapper.feeature.ExecutePointMapper;
import com.zj.domain.repository.feature.IExecutePointRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/17
 */
@Slf4j
@Repository
public class ExecutePointRepository extends ServiceImpl<ExecutePointMapper, ExecutePoint> implements
    IExecutePointRepository {

  @Override
  public ExecutePointDto getExecutePoint(String executePointId) {
    ExecutePoint executePoint = getOne(
        Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getPointId, executePointId));
    return OrikaUtil.convert(executePoint, ExecutePointDto.class);
  }

  @Override
  public List<ExecutePointDto> getExecutePointByFeatureIds(List<String> featureIds) {
    List<ExecutePoint> pointList = list(
        Wrappers.lambdaQuery(ExecutePoint.class).in(ExecutePoint::getFeatureId, featureIds)
            .orderByDesc(ExecutePoint::getCreateTime));

    return OrikaUtil.convertList(pointList, ExecutePointDto.class);
  }

  @Override
  public List<ExecutePointDto> getExecutePointByFeatureId(String featureId) {
    List<ExecutePoint> pointList = list(
        Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getFeatureId, featureId)
            .orderByDesc(ExecutePoint::getCreateTime));

    return OrikaUtil.convertList(pointList, ExecutePointDto.class);
  }

  @Override
  public boolean updateExecutePoint(ExecutePointDto executePointDto) {
    ExecutePoint executePoint = OrikaUtil.convert(executePointDto, ExecutePoint.class);
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
  public List<ExecutePointDto> getPointsByFeatureIds(List<String> featureIds) {
    List<ExecutePoint> executePoints = list(
        Wrappers.lambdaQuery(ExecutePoint.class).in(ExecutePoint::getFeatureId, featureIds)
            .orderByDesc(ExecutePoint::getCreateTime));
    return OrikaUtil.convertList(executePoints, ExecutePointDto.class);
  }

  @Override
  public boolean saveExecutePoint(ExecutePointDto executePointDto) {
    ExecutePoint executePoint = OrikaUtil.convert(executePointDto, ExecutePoint.class);
    long dateNow = System.currentTimeMillis();
    executePoint.setCreateTime(dateNow);
    executePoint.setUpdateTime(dateNow);
    return save(executePoint);
  }

  @Override
  public boolean saveBatch(List<ExecutePointDto> executePoints) {
    List<ExecutePoint> pointList = OrikaUtil.convertList(executePoints, ExecutePoint.class);
    return saveBatch(pointList);
  }


  @Override
  @Transactional
  public boolean updateBatch(List<ExecutePointDto> newExecutePoints) {
    List<ExecutePoint> pointList = OrikaUtil.convertList(newExecutePoints, ExecutePoint.class);
    return updateBatchById(pointList);
  }

  @Override
  public List<ExecutePointDto> getTemplateExecutePoints(String templateId) {
    List<ExecutePoint> executePoints = list(
        Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getTemplateId, templateId));
    return OrikaUtil.convertList(executePoints, ExecutePointDto.class);
  }
}
