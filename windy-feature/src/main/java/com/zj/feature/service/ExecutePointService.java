package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.feature.entity.dto.ExecutePointVo;
import com.zj.feature.entity.type.CompareType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class ExecutePointService{

  @Autowired
  private UniqueIdService uniqueIdService;

  @Autowired
  private IExecutePointRepository executePointRepository;

  public boolean updateByPointId(ExecutePointDto executePoint) {
    return executePointRepository.updateExecutePoint(executePoint);
  }

  public boolean deleteByExecutePointId(String pointId) {
    return executePointRepository.deleteExecutePoint(pointId);
  }

  public boolean deleteByFeatureId(String featureId) {
    return executePointRepository.deleteByFeatureId(featureId);
  }

  private ExecutePointDto getExecutePointById(String executePointId) {
    return executePointRepository.getExecutePoint(executePointId);
  }

  public List<ExecutePointDto> getExecutePointByFeatureIds(List<String> featureIds) {
    return executePointRepository.getPointsByFeatureIds(featureIds);
  }

  public PageSize<ExecutePointVo> queryExecutePointPage(String featureId, int page, int size) {
    Page<ExecutePointDto> result = executePointRepository.queryExecutePointPage(featureId, page, size);
    List<ExecutePointVo> executePointDtos = result.getRecords().stream()
        .map(ExecutePointVo::toExecutePointDTO).collect(Collectors.toList());

    PageSize<ExecutePointVo> detailPageSize = new PageSize<>();
    detailPageSize.setTotal(result.getTotal());
    detailPageSize.setData(executePointDtos);
    return detailPageSize;
  }

  public String createExecutePoint(ExecutePointVo executePointDTO) {
    ExecutePointDto executePoint = ExecutePointVo.toExecutePoint(executePointDTO);
    executePoint.setPointId(uniqueIdService.getUniqueId());
    boolean result = executePointRepository.saveExecutePoint(executePoint);

    log.info("create feature detail result = {}", result);
    return executePoint.getPointId();
  }

  public String updateExecutePoint(ExecutePointVo executePointDTO) {
    ExecutePointDto executePoint = getExecutePointById(executePointDTO.getPointId());
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

  public void batchAddTestFeature(List<ExecutePointVo> executePointDtos) {
    if (CollectionUtils.isEmpty(executePointDtos)) {
      log.warn("batch add test feature is empty list");
      return;
    }

    executePointDtos.forEach(executePointDTO -> {
      ExecutePointDto executePoint = getExecutePointById(executePointDTO.getPointId());
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

  public void saveBatch(List<ExecutePointDto> newExecutePoints) {
    executePointRepository.saveBatch(newExecutePoints);
  }

  public ExecutePointVo getExecutePoint(String executePointId) {
    ExecutePointDto executePoint = getExecutePointById(executePointId);
    return ExecutePointVo.toExecutePointDTO(executePoint);
  }
}
