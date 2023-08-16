package com.zj.service.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.service.service.MicroserviceService;
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

@RequestMapping("/v1/devops")
@RestController
public class MicroserviceRest {

  private final MicroserviceService microservice;

  public MicroserviceRest(MicroserviceService microservice) {
    this.microservice = microservice;
  }

  @ResponseBody
  @GetMapping("/services")
  public ResponseMeta<List<MicroserviceDto>> queryServices() {
    return new ResponseMeta<List<MicroserviceDto>>(ErrorCode.SUCCESS, microservice.getServices());
  }

  @ResponseBody
  @GetMapping("/services/page")
  public ResponseMeta<PageSize<MicroserviceDto>> queryPageServices(@RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size, @RequestParam(value = "name", defaultValue = "") String name) {
    return new ResponseMeta<PageSize<MicroserviceDto>>(ErrorCode.SUCCESS, microservice.getServices(page, size, name));
  }

  @GetMapping("/service/{serviceId}/detail")
  public ResponseMeta<MicroserviceDto> queryServiceDetail(@PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<MicroserviceDto>(ErrorCode.SUCCESS, microservice.queryServiceDetail(serviceId));
  }

  @ResponseBody
  @PostMapping("/services")
  public ResponseMeta<String> createService(@RequestBody MicroserviceDto MicroserviceDto) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS, microservice.createService(MicroserviceDto));
  }

  @ResponseBody
  @PutMapping("/services")
  public ResponseMeta<String> updateService(@RequestBody MicroserviceDto MicroserviceDto) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS, microservice.updateService(MicroserviceDto));
  }

  @ResponseBody
  @DeleteMapping("/service/{serviceId}")
  public ResponseMeta<Boolean> deleteService(@PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, microservice.deleteService(serviceId));
  }
}
