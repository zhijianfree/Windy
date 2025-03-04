package com.zj.feature.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.feature.entity.CompareOperator;
import com.zj.feature.entity.ExecutePointTemplate;
import com.zj.feature.entity.ExecutePointDto;
import com.zj.feature.service.ExecutePointService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/v1/devops/feature")
@RestController
public class ExecutePointRest {
    private final ExecutePointService executePointService;

    public ExecutePointRest(ExecutePointService executePointService) {
        this.executePointService = executePointService;
    }

    @PostMapping("/batch/execute/point")
    public ResponseMeta<Void> batchAddExecutePoint(@Valid @RequestBody List<ExecutePointDto> executePointDtos){
        executePointService.batchAddTestFeature(executePointDtos);
        return new ResponseMeta<>(ErrorCode.SUCCESS);
    }

    @PostMapping("/execute/point")
    public ResponseMeta<String> createExecutePoint(@Valid @RequestBody ExecutePointDto executePointDTO) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.createExecutePoint(executePointDTO));
    }

    @PutMapping("/execute/point")
    public ResponseMeta<String> updateExecutePoint(@Valid @RequestBody ExecutePointDto executePointDTO) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.updateExecutePoint(executePointDTO));
    }

    @DeleteMapping("/execute/point/{executePointId}")
    public ResponseMeta<Boolean> deleteExecutePoint(@PathVariable("executePointId") String executePointId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, executePointService.deleteByExecutePointId(executePointId));
    }

    @DeleteMapping("/{featureId}/execute/points")
    public ResponseMeta<Integer> deleteExecutePointByFeatureId(@PathVariable("featureId") String featureId) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.deleteByFeatureId(featureId));
    }

    @GetMapping("/points/{executePointId}/template")
    public ResponseMeta<ExecutePointTemplate> queryPointTemplate(@PathVariable("executePointId") String executePointId) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.queryPointTemplate(executePointId));
    }

    @GetMapping("/execute/point/{executePointId}")
    public ResponseMeta<ExecutePointDto> queryExecutePoint(@PathVariable("executePointId") String executePointId) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.getExecutePoint(executePointId));
    }

    @GetMapping("/{featureId}/execute/points")
    public ResponseMeta<PageSize<ExecutePointDto>> queryFeaturePoints(@PathVariable("featureId") String featureId) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.getExecutePointsByFeatureId(featureId));
    }

    @GetMapping("/execute/operators")
    public ResponseMeta<List<CompareOperator>> queryExecutePointOperators() {
        return new ResponseMeta<List<CompareOperator>>(ErrorCode.SUCCESS, executePointService.queryExecutePointOperators());
    }
}
