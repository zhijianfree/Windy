package com.zj.feature.rest;

import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.feature.ExecuteRecordBO;
import com.zj.feature.service.ExecuteRecordService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/devops/feature")
@RestController
public class ExecutePointRecordRest {

    private final ExecuteRecordService executeRecordService;

    public ExecutePointRecordRest(ExecuteRecordService executeRecordService) {
        this.executeRecordService = executeRecordService;
    }

    @GetMapping("/{historyId}/records")
    public ResponseMeta<List<ExecuteRecordBO>> queryFeatureHistories(@PathVariable("historyId") String historyId) {
        return new ResponseMeta<List<ExecuteRecordBO>>(ErrorCode.SUCCESS, executeRecordService.getExecuteRecords(historyId));
    }
}
