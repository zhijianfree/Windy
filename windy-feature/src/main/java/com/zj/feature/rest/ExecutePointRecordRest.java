package com.zj.feature.rest;

import com.zj.common.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.dto.ExecuteRecordDTO;
import com.zj.feature.service.ExecuteRecordService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/devops/feature")
@RestController
public class ExecutePointRecordRest {

    @Autowired
    private ExecuteRecordService executeRecordService;

    @GetMapping("/{historyId}/records")
    public ResponseMeta<List<ExecuteRecordDTO>> queryFeatureHistories(@PathVariable("historyId") String historyId) {
        return new ResponseMeta<List<ExecuteRecordDTO>>(ErrorCode.SUCCESS, executeRecordService.getExecuteRecords(historyId));
    }
}
