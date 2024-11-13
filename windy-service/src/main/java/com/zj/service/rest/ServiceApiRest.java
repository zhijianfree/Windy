package com.zj.service.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.feature.ExecuteTemplateVo;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.entity.generate.GenerateRecordBO;
import com.zj.domain.entity.bo.service.ServiceApiBO;
import com.zj.domain.entity.bo.service.ServiceGenerateBO;
import com.zj.service.entity.ApiModel;
import com.zj.service.entity.GenerateTemplate;
import com.zj.service.entity.ImportApiResult;
import com.zj.service.service.ApiService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author falcon
 * @since 2023/8/8
 */
@RestController
@RequestMapping("/v1/devops")
public class ServiceApiRest {

  private final ApiService apiService;

  public ServiceApiRest(ApiService apiService) {
    this.apiService = apiService;
  }

  @GetMapping("/service/resources/{apiId}")
  public ResponseMeta<ServiceApiBO> getServiceApi(@PathVariable("apiId") String apiId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, apiService.getServiceApi(apiId));
  }

  @GetMapping("/service/{serviceId}/resources")
  public ResponseMeta<List<ServiceApiBO>> getServiceApis(
      @PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, apiService.getServiceApis(serviceId));
  }

  @PostMapping("/service/resources")
  public ResponseMeta<Boolean> createServiceApi(@RequestBody ApiModel apiModel) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, apiService.createServiceApi(apiModel));
  }

  @PostMapping("/service/resource/templates")
  public ResponseMeta<List<ExecuteTemplateVo>> apiGenerateTemplate(@RequestBody GenerateTemplate generateTemplate) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, apiService.apiGenerateTemplate(generateTemplate));
  }

  @PutMapping("/service/resources")
  public ResponseMeta<Boolean> updateServiceApi(@RequestBody ApiModel apiModel) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, apiService.updateServiceApi(apiModel));
  }

  @DeleteMapping("/service/resources/{apiId}")
  public ResponseMeta<Boolean> deleteServiceApi(@PathVariable("apiId") String apiId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, apiService.deleteServiceApi(apiId));
  }

  @DeleteMapping("/service/resources")
  public ResponseMeta<Boolean> batchDeleteApi(@RequestParam("apis") List<String> apis) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, apiService.batchDeleteApi(apis));
  }

  @PostMapping("/service/resources/generate")
  public ResponseMeta<Boolean> generateServiceApi(@RequestBody ServiceGenerateBO generate) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, apiService.generateServiceApi(generate));
  }

  @GetMapping("/service/{serviceId}/generate")
  public ResponseMeta<ServiceGenerateBO> getGenerateParams(@PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, apiService.getGenerateParams(serviceId));
  }

  @GetMapping("/service/{serviceId}/generate/log")
  public ResponseMeta<List<GenerateRecordBO>> getLatestGenerateLog(@PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, apiService.getLatestGenerateLog(serviceId));
  }

  @PostMapping(value = "/service/api/import")
  public ResponseMeta<ImportApiResult> importAPIFile(@RequestPart("file") MultipartFile file,
                                                     @RequestPart("type") String fileType,
                                                     @RequestPart("serviceId") String serviceId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, apiService.importApiFile(file, fileType, serviceId));
  }
}
