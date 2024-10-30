package com.zj.feature.service;

import com.alibaba.fastjson.JSON;
import com.zj.common.enums.CompareType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import com.zj.common.uuid.UniqueIdService;
import com.zj.domain.entity.dto.feature.ExecutePointDto;
import com.zj.domain.entity.dto.feature.ExecuteTemplateDto;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.repository.feature.IExecutePointRepository;
import com.zj.domain.repository.feature.IExecuteTemplateRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.feature.entity.CompareOperator;
import com.zj.feature.entity.ExecutePointTemplate;
import com.zj.feature.entity.ExecutePointVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExecutePointService {

    private final UniqueIdService uniqueIdService;
    private final IExecutePointRepository executePointRepository;
    private final IExecuteTemplateRepository templateRepository;
    private final IMicroServiceRepository microServiceRepository;

    public ExecutePointService(UniqueIdService uniqueIdService, IExecutePointRepository executePointRepository, IExecuteTemplateRepository templateRepository, IMicroServiceRepository microServiceRepository) {
        this.uniqueIdService = uniqueIdService;
        this.executePointRepository = executePointRepository;
        this.templateRepository = templateRepository;
        this.microServiceRepository = microServiceRepository;
    }

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

    public String createExecutePoint(ExecutePointVo executePointVo) {
        ExecutePointDto executePoint = toExecutePoint(executePointVo);
        executePoint.setPointId(uniqueIdService.getUniqueId());
        boolean result = executePointRepository.saveExecutePoint(executePoint);

        log.info("create feature detail result = {}", result);
        return executePoint.getPointId();
    }

    public  ExecutePointDto toExecutePoint(ExecutePointVo dto) {
        ExecutePointDto point = new ExecutePointDto();
        point.setFeatureId(dto.getFeatureId());
        point.setPointId(dto.getPointId());
        point.setDescription(dto.getDescription());
        point.setSortOrder(dto.getSortOrder());
        point.setTemplateId(dto.getTemplateId());
        point.setTestStage(dto.getTestStage());
        point.setExecuteType(dto.getExecuteType());
        point.setCompareDefine(JSON.toJSONString(dto.getCompareDefine()));
        point.setVariables(JSON.toJSONString(dto.getVariableDefine()));
        point.setFeatureInfo(JSON.toJSONString(dto.getExecutorUnit()));
        return point;
    }

    public String updateExecutePoint(ExecutePointVo executePointVo) {
        ExecutePointDto executePoint = getExecutePointById(executePointVo.getPointId());
        if (Objects.isNull(executePoint)) {
            return null;
        }

        executePoint.setTestStage(executePointVo.getTestStage());
        executePoint.setDescription(executePointVo.getDescription());
        executePoint.setSortOrder(executePointVo.getSortOrder());
        executePoint.setExecuteType(executePointVo.getExecuteType());
        executePoint.setFeatureInfo(JSON.toJSONString(executePointVo.getExecutorUnit()));
        executePoint.setCompareDefine(JSON.toJSONString(executePointVo.getCompareDefine()));
        executePoint.setVariables(JSON.toJSONString(executePointVo.getVariableDefine()));
        executePoint.setUpdateTime(System.currentTimeMillis());
        boolean result = updateByPointId(executePoint);

        log.info("update feature detail result = {}", result);
        return executePoint.getPointId();
    }

    public void batchAddTestFeature(List<ExecutePointVo> executePoints) {
        if (CollectionUtils.isEmpty(executePoints)) {
            log.warn("batch add test feature is empty list");
            return;
        }

        executePoints.forEach(executePointDTO -> {
            ExecutePointDto executePoint = getExecutePointById(executePointDTO.getPointId());
            if (Objects.isNull(executePoint)) {
                createExecutePoint(executePointDTO);
            } else {
                updateExecutePoint(executePointDTO);
            }
        });
    }

    public List<CompareOperator> queryExecutePointOperators() {
        return Arrays.stream(CompareType.values()).map(type ->{
            CompareOperator compareOperator = new CompareOperator();
            compareOperator.setOperator(type.getOperator());
            compareOperator.setDescription(type.getDesc());
            return compareOperator;
        }).collect(Collectors.toList());
    }

    public void saveBatch(List<ExecutePointDto> newExecutePoints) {
        executePointRepository.saveBatch(newExecutePoints);
    }

    public ExecutePointVo getExecutePoint(String executePointId) {
        ExecutePointDto executePoint = getExecutePointById(executePointId);
        return ExecutePointVo.toExecutePointDTO(executePoint);
    }

    public List<ExecutePointVo> getExecutePointsByFeatureId(String featureId) {
        List<ExecutePointDto> executePoints = executePointRepository.getExecutePointByFeatureId(featureId);
        return executePoints.stream().map(ExecutePointVo::toExecutePointDTO)
                .sorted(Comparator.comparing(ExecutePointVo::getSortOrder)).collect(Collectors.toList());
    }

    public ExecutePointTemplate queryPointTemplate(String executePointId) {
        ExecutePointDto executePoint = executePointRepository.getExecutePoint(executePointId);
        if (Objects.isNull(executePoint)) {
            log.info("can not find execute point = {}", executePointId);
            throw new ApiException(ErrorCode.EXECUTE_POINT_NOT_FIND);
        }

        ExecuteTemplateDto executeTemplate = templateRepository.getExecuteTemplate(executePoint.getTemplateId());
        if (Objects.isNull(executeTemplate)) {
            log.info("can not find template = {}", executePoint.getTemplateId());
            throw new ApiException(ErrorCode.TEMPLATE_NOT_FIND);
        }

        ExecutePointTemplate executePointTemplate = new ExecutePointTemplate();
        executePointTemplate.setInvokeType(executeTemplate.getInvokeType());
        executePointTemplate.setRequest(executeTemplate.getService());
        executePointTemplate.setMethod(executeTemplate.getMethod());
        executePointTemplate.setDescription(executeTemplate.getDescription());
        MicroserviceDto microserviceDto = microServiceRepository.queryServiceDetail(executeTemplate.getOwner());
        if (Objects.nonNull(microserviceDto)) {
            executePointTemplate.setService(microserviceDto.getServiceName());
        }
        return executePointTemplate;
    }
}
