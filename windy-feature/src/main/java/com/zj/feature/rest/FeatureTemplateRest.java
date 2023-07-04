package com.zj.feature.rest;

import com.zj.common.model.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.dto.ExecuteTemplateVo;
import com.zj.common.model.PageSize;
import com.zj.feature.service.TemplateService;
import java.util.List;
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

  private TemplateService templateService;

  public FeatureTemplateRest(TemplateService templateService) {
    this.templateService = templateService;
  }

  @ResponseBody
  @GetMapping("/templates/page")
  public ResponseMeta<PageSize<ExecuteTemplateVo>> getTemplatePage(
      @RequestParam(value = "page", defaultValue = "1") Integer page, @RequestParam(value = "size", defaultValue = "10") Integer size,
      @RequestParam(value = "name", defaultValue = "") String name) {
    PageSize<ExecuteTemplateVo> featureConfigs = templateService.getTemplatePage(page, size, name);
    return new ResponseMeta(ErrorCode.SUCCESS, featureConfigs);
  }

  @ResponseBody
  @GetMapping("/templates")
  public ResponseMeta<List<ExecuteTemplateVo>> getFeatureTemplates() {
    List<ExecuteTemplateVo> featureConfigs = templateService.getFeatureList();
    return new ResponseMeta(ErrorCode.SUCCESS, featureConfigs);
  }

  @ResponseBody
  @PostMapping("/template")
  public ResponseMeta<String> createFeatureTemplate(
      @RequestBody ExecuteTemplateVo executeTemplate) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS,
        templateService.createTemplate(executeTemplate));
  }

  @ResponseBody
  @PutMapping("/template")
  public ResponseMeta<String> updateFeatureTemplate(@RequestBody ExecuteTemplateVo executeTemplate) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS,
        templateService.updateTemplate(executeTemplate));
  }

  @ResponseBody
  @GetMapping("/template/{templateId}")
  public ResponseMeta<ExecuteTemplateVo> getExecuteTemplate(@PathVariable("templateId") String templateId) {
    ExecuteTemplateVo featureConfig = templateService.getExecuteTemplate(templateId);
    return new ResponseMeta(ErrorCode.SUCCESS, featureConfig);
  }

  @ResponseBody
  @DeleteMapping("/template/{templateId}")
  public ResponseMeta<Boolean> updateExecuteTemplate(@PathVariable("templateId") String templateId) {
    Boolean result = templateService.deleteExecuteTemplate(templateId);
    return new ResponseMeta(ErrorCode.SUCCESS, result);
  }

  @ResponseBody
  @PutMapping("/{templateId}/refresh")
  public ResponseMeta<Boolean> refreshTemplate(@PathVariable("templateId") String templateId) {
    Boolean result = templateService.refreshTemplate(templateId);
    return new ResponseMeta(ErrorCode.SUCCESS, result);
  }
}
