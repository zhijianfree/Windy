package com.zj.feature.rest;

import com.zj.common.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.dto.ExecutePointDTO;
import com.zj.feature.entity.dto.PageSize;
import com.zj.feature.service.ExecutePointService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/devops")
@RestController
public class ExecutePointRest {
    @Autowired
    private ExecutePointService executePointService;

    @PostMapping("/feature/batch/execute/point")
    public ResponseMeta<Void> batchAddExecutePoint(@Valid @RequestBody List<ExecutePointDTO> executePointDTOS){
        executePointService.batchAddTestFeature(executePointDTOS);
        return new ResponseMeta<>(ErrorCode.SUCCESS);
    }

    @PostMapping("/feature/execute/point")
    public ResponseMeta<String> createExecutePoint(@Valid @RequestBody ExecutePointDTO executePointDTO) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.createExecutePoint(executePointDTO));
    }

    @PutMapping("/feature/execute/point")
    public ResponseMeta<String> updateExecutePoint(@Valid @RequestBody ExecutePointDTO executePointDTO) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.updateExecutePoint(executePointDTO));
    }

    @DeleteMapping("/feature/execute/point/{executePointId}")
    public ResponseMeta<Boolean> deleteExecutePoint(@PathVariable("executePointId") String executePointId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, executePointService.deleteByExecutePointId(executePointId));
    }

    @DeleteMapping("/feature/{featureId}/execute/points")
    public ResponseMeta<Integer> deleteExecutePointByFeatureId(@PathVariable("featureId") String featureId) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.deleteByFeatureId(featureId));
    }

    @GetMapping("/feature/execute/point/{executePointId}")
    public ResponseMeta<ExecutePointDTO> queryExecutePoint(@PathVariable("executePointId") String executePointId) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.getExecutePointDTO(executePointId));
    }

    @GetMapping("/feature/{featureId}/execute/points")
    public ResponseMeta<PageSize<ExecutePointDTO>> queryExecutePoint(@PathVariable("featureId") String featureId, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "size", defaultValue = "10") int size) {
        return new ResponseMeta(ErrorCode.SUCCESS, executePointService.queryExecutePointPage(featureId, page, size));
    }

    @GetMapping("/feature/execute/operators")
    public ResponseMeta<List<String>> queryExecutePointOperators() {
        return new ResponseMeta<List<String>>(ErrorCode.SUCCESS, executePointService.queryExecutePointOperators());
    }
}
