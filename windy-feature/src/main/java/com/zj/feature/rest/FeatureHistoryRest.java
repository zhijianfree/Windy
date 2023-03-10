package com.zj.feature.rest;

import com.zj.common.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.dto.FeatureHistoryDTO;
import com.zj.feature.service.FeatureHistoryService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/devops")
@RestController
public class FeatureHistoryRest {

    @Autowired
    private FeatureHistoryService featureHistoryService;

    @GetMapping("/feature/{featureId}/histories")
    public ResponseMeta<List<FeatureHistoryDTO>> queryFeatureHistories(@PathVariable("featureId") String featureId) {
        return new ResponseMeta<List<FeatureHistoryDTO>>(ErrorCode.SUCCESS, featureHistoryService.featureHistories(featureId));
    }


    @DeleteMapping("/feature/history/{historyId}")
    public ResponseMeta<Boolean> deleteFeatureHistory(@PathVariable("historyId") String historyId) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, featureHistoryService.deleteHistory(historyId));
    }
}
