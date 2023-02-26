package com.zj.feature.rest;

import com.zj.common.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.dto.ExecuteTemplateDTO;
import com.zj.feature.entity.dto.PageSize;
import com.zj.feature.service.FeatureConfigService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/devops/feature")
@RestController
public class FeatureTemplateRest {

  @Autowired
  private FeatureConfigService featureConfigService;

  @ResponseBody
  @GetMapping("/templates/page")
  public ResponseMeta<PageSize<ExecuteTemplateDTO>> getFeaturePage(
      @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "size", defaultValue = "10") Integer size,
      @RequestParam(value = "name", defaultValue = "") String name) {
    PageSize<ExecuteTemplateDTO> featureConfigs = featureConfigService.getFeaturePage(page, size, name);
    return new ResponseMeta(ErrorCode.SUCCESS, featureConfigs);
  }

  @ResponseBody
  @GetMapping("/templates")
  public ResponseMeta<List<ExecuteTemplateDTO>> getFeatureTemplates() {
    List<ExecuteTemplateDTO> featureConfigs = featureConfigService.getFeatureList();
    return new ResponseMeta(ErrorCode.SUCCESS, featureConfigs);
  }

  @ResponseBody
  @PostMapping("/template")
  public ResponseMeta<String> createFeatureTemplate(
      @RequestBody ExecuteTemplateDTO executeTemplate) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS,
        featureConfigService.createTemplate(executeTemplate));
  }

  @ResponseBody
  @PutMapping("/template")
  public ResponseMeta<String> updateFeatureTemplate(@RequestBody ExecuteTemplateDTO executeTemplate) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS,
        featureConfigService.updateTemplate(executeTemplate));
  }

  @ResponseBody
  @GetMapping("/template/{templateId}")
  public ResponseMeta<ExecuteTemplateDTO> getExecuteTemplate(
      @PathVariable("templateId") String templateId) {
    ExecuteTemplateDTO featureConfig = featureConfigService.getExecuteTemplate(templateId);
    return new ResponseMeta(ErrorCode.SUCCESS, featureConfig);
  }

  @ResponseBody
  @DeleteMapping("/template/{templateId}")
  public ResponseMeta<Boolean> updateExecuteTemplate(
      @PathVariable("templateId") String templateId) {
    Boolean result = featureConfigService.deleteExecuteTemplate(templateId);
    return new ResponseMeta(ErrorCode.SUCCESS, result);
  }
}
