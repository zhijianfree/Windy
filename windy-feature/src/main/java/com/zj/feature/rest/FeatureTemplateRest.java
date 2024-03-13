package com.zj.feature.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.feature.entity.dto.BatchTemplates;
import com.zj.common.feature.ExecuteTemplateVo;
import com.zj.feature.entity.dto.UploadResultDto;
import com.zj.feature.service.TemplateService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/v1/devops/feature")
@RestController
public class FeatureTemplateRest {

    private final TemplateService templateService;

    public FeatureTemplateRest(TemplateService templateService) {
        this.templateService = templateService;
    }

    @ResponseBody
    @GetMapping("/{serviceId}/templates/page")
    public ResponseMeta<PageSize<ExecuteTemplateVo>> getTemplatePage(@PathVariable("serviceId") String serviceId,
                                                                     @RequestParam(value = "page",
                                                                             defaultValue = "1") Integer page,
                                                                     @RequestParam(value = "size", defaultValue = "10"
                                                                     ) Integer size, @RequestParam(value = "name",
            defaultValue = "") String name) {
        PageSize<ExecuteTemplateVo> featureConfigs = templateService.getTemplatePage(serviceId, page, size, name);
        return new ResponseMeta<>(ErrorCode.SUCCESS, featureConfigs);
    }

    @ResponseBody
    @GetMapping("/types/{invokeType}/templates")
    public ResponseMeta<List<ExecuteTemplateVo>> getTemplatesByInvokeType(@PathVariable("invokeType") Integer invokeType) {
        List<ExecuteTemplateVo> executeTemplates = templateService.getTemplatesByInvokeType(invokeType);
        return new ResponseMeta<>(ErrorCode.SUCCESS, executeTemplates);
    }

    @ResponseBody
    @GetMapping("/{serviceId}/templates")
    public ResponseMeta<List<ExecuteTemplateVo>> getFeatureTemplates(@PathVariable("serviceId") String serviceId) {
        List<ExecuteTemplateVo> featureConfigs = templateService.getFeatureList(serviceId);
        return new ResponseMeta<>(ErrorCode.SUCCESS, featureConfigs);
    }

    @ResponseBody
    @PostMapping("/template")
    public ResponseMeta<String> createFeatureTemplate(@RequestBody ExecuteTemplateVo executeTemplate) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, templateService.createTemplate(executeTemplate));
    }

    @ResponseBody
    @PostMapping("/templates")
    public ResponseMeta<Boolean> batchCreateTemplates(@RequestBody @Validated BatchTemplates batchTemplates) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, templateService.batchCreateTemplates(batchTemplates));
    }

    @ResponseBody
    @PutMapping("/template")
    public ResponseMeta<String> updateFeatureTemplate(@RequestBody ExecuteTemplateVo executeTemplate) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, templateService.updateTemplate(executeTemplate));
    }

    @ResponseBody
    @GetMapping("/template/{templateId}")
    public ResponseMeta<ExecuteTemplateVo> getExecuteTemplate(@PathVariable("templateId") String templateId) {
        ExecuteTemplateVo featureConfig = templateService.getExecuteTemplate(templateId);
        return new ResponseMeta<>(ErrorCode.SUCCESS, featureConfig);
    }

    @ResponseBody
    @DeleteMapping("/template/{templateId}")
    public ResponseMeta<Boolean> updateExecuteTemplate(@PathVariable("templateId") String templateId) {
        Boolean result = templateService.deleteExecuteTemplate(templateId);
        return new ResponseMeta<>(ErrorCode.SUCCESS, result);
    }

    @ResponseBody
    @PutMapping("/{templateId}/refresh")
    public ResponseMeta<Boolean> refreshTemplate(@PathVariable("templateId") String templateId) {
        Boolean result = templateService.refreshTemplate(templateId);
        return new ResponseMeta<>(ErrorCode.SUCCESS, result);
    }

    @PostMapping(value = "/template/upload")
    public ResponseMeta<UploadResultDto> uploadTemplate(@RequestPart("file") MultipartFile file,
                                                        @RequestPart("serviceId") String serviceId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, templateService.uploadTemplate(file, serviceId));
    }

    @DeleteMapping(value = "/plugin/{pluginId}")
    public ResponseMeta<Boolean> deletePlugin(@PathVariable("pluginId") String pluginId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, templateService.deletePlugin(pluginId));
    }
}
