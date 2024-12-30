package com.zj.feature.rest;

import com.zj.common.entity.dto.PageSize;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.BatchTemplates;
import com.zj.feature.entity.ExecuteTemplateDto;
import com.zj.feature.entity.RelatedTemplateDto;
import com.zj.feature.entity.UploadResultDto;
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
    public ResponseMeta<PageSize<ExecuteTemplateDto>> getTemplatePage(@PathVariable("serviceId") String serviceId,
                                                                      @RequestParam(value = "page",
                                                                             defaultValue = "1") Integer page,
                                                                      @RequestParam(value = "size", defaultValue = "10"
                                                                     ) Integer size, @RequestParam(value = "name",
            defaultValue = "") String name) {
        PageSize<ExecuteTemplateDto> featureConfigs = templateService.getTemplatePage(serviceId, page, size, name);
        return new ResponseMeta<>(ErrorCode.SUCCESS, featureConfigs);
    }
    @ResponseBody
    @GetMapping("/templates")
    public ResponseMeta<List<ExecuteTemplateDto>> getAllTemplates() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, templateService.getAllTemplates());
    }

    @ResponseBody
    @GetMapping("/types/{invokeType}/templates")
    public ResponseMeta<List<ExecuteTemplateDto>> getTemplatesByInvokeType(@PathVariable("invokeType") Integer invokeType) {
        List<ExecuteTemplateDto> executeTemplates = templateService.getTemplatesByInvokeType(invokeType);
        return new ResponseMeta<>(ErrorCode.SUCCESS, executeTemplates);
    }

    @ResponseBody
    @PostMapping("/template")
    public ResponseMeta<String> createFeatureTemplate(@RequestBody ExecuteTemplateDto executeTemplate) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, templateService.createTemplate(executeTemplate));
    }

    @ResponseBody
    @PostMapping("/templates")
    public ResponseMeta<Boolean> batchCreateTemplates(@RequestBody @Validated BatchTemplates batchTemplates) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, templateService.batchCreateTemplates(batchTemplates));
    }

    @ResponseBody
    @PostMapping("/related/templates")
    public ResponseMeta<Boolean> addRelatedTemplate(@Validated @RequestBody RelatedTemplateDto relatedTemplateDto) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, templateService.addRelatedTemplate(relatedTemplateDto));
    }

    @ResponseBody
    @GetMapping("/{serviceId}/related/templates")
    public ResponseMeta<List<ExecuteTemplateDto>> serviceRelatedTemplates(@PathVariable("serviceId") String serviceId) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, templateService.serviceRelatedTemplates(serviceId));
    }

    @ResponseBody
    @PutMapping("/template")
    public ResponseMeta<String> updateFeatureTemplate(@RequestBody ExecuteTemplateDto executeTemplate) {
        return new ResponseMeta<>(ErrorCode.SUCCESS, templateService.updateTemplate(executeTemplate));
    }

    @ResponseBody
    @GetMapping("/template/{templateId}")
    public ResponseMeta<ExecuteTemplateDto> getExecuteTemplate(@PathVariable("templateId") String templateId) {
        ExecuteTemplateDto featureConfig = templateService.getExecuteTemplate(templateId);
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
