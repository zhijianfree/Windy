package com.zj.feature.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.feature.BatchDeleteDto;
import com.zj.domain.entity.po.feature.FeatureInfo;
import com.zj.feature.entity.dto.CopyFeatureDto;
import com.zj.feature.entity.dto.FeatureInfoDto;
import com.zj.feature.entity.dto.FeatureNodeDto;
import com.zj.feature.entity.dto.TagFilterDto;
import com.zj.feature.service.FeatureService;
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
public class FeatureInfoRest {

  @Autowired
  private FeatureService featureService;

  @GetMapping("/{caseId}/tree/features")
  public ResponseMeta<List<FeatureInfo>> getFeatureTreeList(
      @PathVariable("caseId") String caseId) {
    return new ResponseMeta(ErrorCode.SUCCESS,
        featureService.getFeatureTreeList(caseId));
  }

  @PostMapping("/feature")
  public ResponseMeta<String> createFeature(@Valid @RequestBody FeatureInfoDto featureInfoDTO) {
    return new ResponseMeta(ErrorCode.SUCCESS, featureService.createFeature(featureInfoDTO));
  }

  @PutMapping("/feature")
  public ResponseMeta<String> updateFeature(@Valid @RequestBody FeatureInfoDto featureInfoDTO) {
    return new ResponseMeta(ErrorCode.SUCCESS, featureService.updateFeatureInfo(featureInfoDTO));
  }

  @DeleteMapping("/feature/{featureId}")
  public ResponseMeta<Boolean> deleteFeature(@PathVariable("featureId") String featureId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, featureService.deleteByFeatureId(featureId));
  }

  @PostMapping("/delete/features")
  public ResponseMeta<Boolean> batchDeleteFeature(@RequestBody BatchDeleteDto batchDeleteDTO) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, featureService.batchDeleteByFeatureId(batchDeleteDTO));
  }

  @GetMapping("/feature/{featureId}")
  public ResponseMeta<FeatureInfoDto> queryFeature(@PathVariable("featureId") String featureId) {
    return new ResponseMeta<FeatureInfoDto>(ErrorCode.SUCCESS,
        featureService.getFeatureById(featureId));
  }

  @GetMapping("/case/{caseId}/features")
  public ResponseMeta<PageSize<com.zj.domain.entity.dto.feature.FeatureInfoDto>> queryFeatureTask(
      @PathVariable("caseId") String caseId,
      @RequestParam(value = "page", defaultValue = "1") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {
    return new ResponseMeta(ErrorCode.SUCCESS,
        featureService.queryFeaturePage(caseId, page, size));
  }

  @PostMapping("/feature/start/{featureId}")
  public ResponseMeta<String> startFeatureTask(@PathVariable("featureId") String featureId) {
    return new ResponseMeta(ErrorCode.SUCCESS, featureService.executeFeature(featureId));
  }

  @PostMapping("/feature/tag/filter")
  public ResponseMeta<List<FeatureNodeDto>> getFeaturesByTag(
      @Valid @RequestBody TagFilterDto tagFilterDTO) {
    return new ResponseMeta<List<FeatureNodeDto>>(ErrorCode.SUCCESS,
        featureService.filterFeaturesByTag(tagFilterDTO));
  }

  @PostMapping("/feature/copy")
  public ResponseMeta<Boolean> copyFeatures(@Valid @RequestBody CopyFeatureDto copyFeatureDTO) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, featureService.copyFeatures(copyFeatureDTO));
  }
}
