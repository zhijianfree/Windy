package com.zj.feature.rest;

import com.zj.common.model.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.dto.feature.FeatureHistoryDto;
import com.zj.feature.service.FeatureHistoryService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/devops/feature")
@RestController
public class FeatureHistoryRest {

    private FeatureHistoryService featureHistoryService;

    public FeatureHistoryRest(FeatureHistoryService featureHistoryService) {
        this.featureHistoryService = featureHistoryService;
    }

    @GetMapping("/{featureId}/histories")
    public ResponseMeta<List<FeatureHistoryDto>> queryFeatureHistories(@PathVariable("featureId") String featureId) {
        return new ResponseMeta<List<FeatureHistoryDto>>(ErrorCode.SUCCESS, featureHistoryService.featureHistories(featureId));
    }


    @DeleteMapping("/history/{historyId}")
    public ResponseMeta<Boolean> deleteFeatureHistory(@PathVariable("historyId") String historyId) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, featureHistoryService.deleteHistory(historyId));
    }
}
