package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.dto.ExecutePointDTO;
import com.zj.feature.entity.dto.PageSize;
import com.zj.feature.entity.po.ExecutePoint;
import com.zj.feature.executor.compare.CompareType;
import com.zj.feature.mapper.ExecutePointMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class ExecutePointService extends ServiceImpl<ExecutePointMapper, ExecutePoint> {

  public boolean updateByPointId(ExecutePoint executePoint) {
    return update(executePoint, Wrappers.lambdaUpdate(ExecutePoint.class)
        .eq(ExecutePoint::getPointId, executePoint.getPointId()));
  }

  public boolean deleteByExecutePointId(String testId) {
    return remove(Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getPointId, testId));
  }

  public boolean deleteByFeatureId(String featureId) {
    return remove(
        Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getFeatureId, featureId));
  }

  public ExecutePointDTO getExecutePointDTO(String executePointId) {
    ExecutePoint executePoint = getExecutePointById(executePointId);
    if (Objects.isNull(executePoint)) {
      throw new ApiException(ErrorCode.EXECUTE_POINT_NOT_FIND);
    }

    return ExecutePointDTO.toExecutePointDTO(executePoint);
  }

  private ExecutePoint getExecutePointById(String executePointId) {
    return getOne(
        Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getPointId, executePointId));
  }

  public List<ExecutePoint> getExecutePointByFeatureIds(List<String> featureIds) {
    return list(Wrappers.lambdaQuery(ExecutePoint.class).in(ExecutePoint::getFeatureId, featureIds)
        .orderByDesc(ExecutePoint::getCreateTime));
  }

  public PageSize<ExecutePointDTO> queryExecutePointPage(String featureId, int page, int size) {
    Page<ExecutePoint> pageSize = new Page<>(page, size);
    Page<ExecutePoint> result = page(pageSize,
        Wrappers.lambdaQuery(ExecutePoint.class).eq(ExecutePoint::getFeatureId, featureId)
            .orderByAsc(ExecutePoint::getSortOrder));

    List<ExecutePointDTO> executePointDTOS = result.getRecords().stream()
        .map(ExecutePointDTO::toExecutePointDTO).collect(Collectors.toList());

    PageSize<ExecutePointDTO> detailPageSize = new PageSize<>();
    detailPageSize.setTotal(result.getTotal());
    detailPageSize.setData(executePointDTOS);
    return detailPageSize;
  }

  public String createExecutePoint(ExecutePointDTO executePointDTO) {
    ExecutePoint executePoint = ExecutePointDTO.toExecutePoint(executePointDTO);
    executePoint.setPointId(UUID.randomUUID().toString());
    executePoint.setCreateTime(System.currentTimeMillis());
    executePoint.setUpdateTime(System.currentTimeMillis());
    boolean result = save(executePoint);

    log.info("create feature detail result = {}", result);
    return executePoint.getPointId();
  }

  public String updateExecutePoint(ExecutePointDTO executePointDTO) {
    ExecutePoint executePoint = getExecutePointById(executePointDTO.getPointId());
    if (Objects.isNull(executePoint)) {
      return null;
    }

    executePoint.setTestStage(executePointDTO.getTestStage());
    executePoint.setDescription(executePointDTO.getDescription());
    executePoint.setSortOrder(executePointDTO.getSortOrder());
    executePoint.setExecuteType(executePointDTO.getExecuteType());
    executePoint.setFeatureInfo(JSON.toJSONString(executePointDTO.getExecutorUnit()));
    executePoint.setCompareDefine(JSON.toJSONString(executePointDTO.getCompareDefine()));
    executePoint.setVariables(JSON.toJSONString(executePointDTO.getVariableDefine()));
    executePoint.setUpdateTime(System.currentTimeMillis());
    boolean result = updateByPointId(executePoint);

    log.info("update feature detail result = {}", result);
    return executePoint.getPointId();
  }

  public void batchAddTestFeature(List<ExecutePointDTO> executePointDTOS) {
    if (CollectionUtils.isEmpty(executePointDTOS)) {
      log.warn("batch add test feature is empty list");
      return;
    }

    executePointDTOS.forEach(executePointDTO -> {
      ExecutePoint executePoint = getExecutePointById(executePointDTO.getPointId());
      if (Objects.isNull(executePoint)) {
        createExecutePoint(executePointDTO);
      } else {
        updateExecutePoint(executePointDTO);
      }
    });
  }

  public List<String> queryExecutePointOperators() {
    return Arrays.stream(CompareType.values()).map(CompareType::getOperator)
        .collect(Collectors.toList());
  }
}
