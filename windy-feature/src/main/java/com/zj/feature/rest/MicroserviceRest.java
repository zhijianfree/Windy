package com.zj.feature.rest;

import com.zj.common.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.feature.entity.dto.MicroserviceDTO;
import com.zj.feature.entity.dto.PageSize;
import com.zj.feature.service.MicroserviceService;
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

@RequestMapping("/v1/devops")
@RestController
public class MicroserviceRest {

  @Autowired
  private MicroserviceService microservice;

  @ResponseBody
  @GetMapping("/services")
  public ResponseMeta<List<MicroserviceDTO>> queryServices() {
    return new ResponseMeta<List<MicroserviceDTO>>(ErrorCode.SUCCESS, microservice.getServices());
  }

  @ResponseBody
  @GetMapping("/services/page")
  public ResponseMeta<PageSize<MicroserviceDTO>> queryPageServices(@RequestParam(value = "page", defaultValue = "1") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size, @RequestParam(value = "name", defaultValue = "") String name) {
    return new ResponseMeta<PageSize<MicroserviceDTO>>(ErrorCode.SUCCESS, microservice.getServices(page, size, name));
  }

  @ResponseBody
  @PostMapping("/services")
  public ResponseMeta<String> createService(@RequestBody MicroserviceDTO microserviceDto) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS, microservice.createService(microserviceDto));
  }

  @ResponseBody
  @PutMapping("/services")
  public ResponseMeta<String> updateService(@RequestBody MicroserviceDTO microserviceDto) {
    return new ResponseMeta<String>(ErrorCode.SUCCESS, microservice.updateService(microserviceDto));
  }

  @ResponseBody
  @DeleteMapping("/service/{serviceId}")
  public ResponseMeta<Integer> deleteService(@PathVariable("serviceId") String serviceId) {
    return new ResponseMeta<Integer>(ErrorCode.SUCCESS, microservice.deleteService(serviceId));
  }
}
