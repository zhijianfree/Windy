package com.zj.domain.repository.feature.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.po.feature.ExecutePoint;
import com.zj.domain.mapper.feeature.ExecutePointMapper;
import com.zj.domain.repository.feature.IExecutePointRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

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
}
