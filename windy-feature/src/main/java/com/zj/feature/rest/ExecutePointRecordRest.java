package com.zj.feature.rest;

import com.zj.common.model.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.dto.feature.ExecuteRecordDto;
import com.zj.feature.service.ExecuteRecordService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/devops/feature")
@RestController
public class ExecutePointRecordRest {

    private ExecuteRecordService executeRecordService;

    public ExecutePointRecordRest(ExecuteRecordService executeRecordService) {
        this.executeRecordService = executeRecordService;
    }

    @GetMapping("/{historyId}/records")
    public ResponseMeta<List<ExecuteRecordDto>> queryFeatureHistories(@PathVariable("historyId") String historyId) {
        return new ResponseMeta<List<ExecuteRecordDto>>(ErrorCode.SUCCESS, executeRecordService.getExecuteRecords(historyId));
    }
}
