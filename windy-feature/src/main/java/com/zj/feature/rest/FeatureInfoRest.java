package com.zj.feature.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.feature.FeatureInfoDto;
import com.zj.feature.entity.dto.BatchDeleteDto;
import com.zj.feature.entity.dto.BatchUpdateFeatures;
import com.zj.feature.entity.dto.CopyCaseFeatureDto;
import com.zj.feature.entity.dto.FeatureInfoVo;
import com.zj.feature.entity.dto.FeatureNodeDto;
import com.zj.feature.entity.dto.PasteFeatureDto;
import com.zj.feature.entity.dto.TagFilterDto;
import com.zj.feature.service.FeatureService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/v1/devops")
@RestController
public class FeatureInfoRest {

    private final FeatureService featureService;

    public FeatureInfoRest(FeatureService featureService) {
        this.featureService = featureService;
    }

    @GetMapping("/{caseId}/tree/features")
    public ResponseMeta<List<FeatureNodeDto>> getFeatureTreeList(
            @PathVariable("caseId") String caseId) {
        return new ResponseMeta(ErrorCode.SUCCESS, featureService.getFeatureTreeList(caseId));
    }

    @GetMapping("/{caseId}/features")
    public ResponseMeta<List<FeatureInfoDto>> getFeatureList(@PathVariable("caseId") String caseId) {
        return new ResponseMeta(ErrorCode.SUCCESS, featureService.queryFeatureList(caseId));
    }

    @PostMapping("/feature")
    public ResponseMeta<String> createFeature(@Valid @RequestBody FeatureInfoVo featureInfoDTO) {
        return new ResponseMeta(ErrorCode.SUCCESS, featureService.createFeature(featureInfoDTO));
    }

    @PutMapping("/feature")
    public ResponseMeta<String> updateFeature(@RequestBody FeatureInfoVo featureInfoVo) {
        return new ResponseMeta(ErrorCode.SUCCESS, featureService.updateFeatureInfo(featureInfoVo));
    }

    @DeleteMapping("/feature/{featureId}")
    public ResponseMeta<Boolean> deleteFeature(@PathVariable("featureId") String featureId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, featureService.deleteByFeatureId(featureId));
    }

    @PutMapping("/batch/features")
    public ResponseMeta<Boolean> batchUpdateFeatures(@RequestBody BatchUpdateFeatures batchUpdateFeatures) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, featureService.batchUpdateFeatures(batchUpdateFeatures));
    }

    @DeleteMapping("/delete/features")
    public ResponseMeta<Boolean> batchDeleteFeature(@RequestBody BatchDeleteDto batchDeleteDTO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, featureService.batchDeleteByFeatureId(batchDeleteDTO));
    }

    @GetMapping("/feature/{featureId}")
    public ResponseMeta<FeatureInfoVo> queryFeature(@PathVariable("featureId") String featureId) {
        return new ResponseMeta<FeatureInfoVo>(ErrorCode.SUCCESS, featureService.getFeatureById(featureId));
    }

    @GetMapping("/case/{caseId}/features")
    public ResponseMeta<PageSize<FeatureInfoDto>> queryFeatureTask(
            @PathVariable("caseId") String caseId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return new ResponseMeta(ErrorCode.SUCCESS, featureService.queryFeaturePage(caseId, page, size));
    }

    @PostMapping("/feature/start/{featureId}")
    public ResponseMeta<String> startFeatureTask(@PathVariable("featureId") String featureId) {
        return new ResponseMeta(ErrorCode.SUCCESS, featureService.executeFeature(featureId));
    }

    @PostMapping("/feature/tag/filter")
    public ResponseMeta<List<FeatureNodeDto>> getFeaturesByTag(@Valid @RequestBody TagFilterDto tagFilterDTO) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, featureService.filterFeaturesByTag(tagFilterDTO));
    }

    @PostMapping("/feature/case/copy")
    public ResponseMeta<Boolean> copyCaseFeatures(@Valid @RequestBody CopyCaseFeatureDto copyCaseFeatures) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, featureService.copyCaseFeatures(copyCaseFeatures));
    }

    @PostMapping("/feature/paste")
    public ResponseMeta<Boolean> pasteFeatures(@Valid @RequestBody PasteFeatureDto copyFeature) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, featureService.pasteFeatures(copyFeature));
    }
}
