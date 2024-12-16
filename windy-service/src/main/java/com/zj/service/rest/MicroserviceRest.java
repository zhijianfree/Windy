package com.zj.service.rest;

import com.zj.common.entity.dto.PageSize;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.domain.entity.bo.auth.UserBO;
import com.zj.domain.entity.bo.service.BuildToolBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.service.entity.ServiceDto;
import com.zj.service.entity.ServiceMemberDto;
import com.zj.service.entity.ServiceStaticsDto;
import com.zj.service.entity.SystemBuildDto;
import com.zj.service.service.MicroserviceService;
import org.springframework.validation.annotation.Validated;
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

import java.util.List;

@RequestMapping("/v1/devops")
@RestController
public class MicroserviceRest {

    private final MicroserviceService microservice;

    public MicroserviceRest(MicroserviceService microservice) {
        this.microservice = microservice;
    }

    @ResponseBody
    @GetMapping("/services")
    public ResponseMeta<List<MicroserviceBO>> queryServices() {
        return new ResponseMeta<List<MicroserviceBO>>(ErrorCode.SUCCESS, microservice.getServices());
    }

    @ResponseBody
    @GetMapping("/services/page")
    public ResponseMeta<PageSize<ServiceDto>> queryPageServices(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                @RequestParam(value = "size", defaultValue = "10") Integer size, @RequestParam(value = "name", defaultValue = "") String name) {
        return new ResponseMeta<PageSize<ServiceDto>>(ErrorCode.SUCCESS, microservice.getServices(page, size, name));
    }

    @GetMapping("/service/{serviceId}/detail")
    public ResponseMeta<MicroserviceBO> queryServiceDetail(@PathVariable("serviceId") String serviceId) {
        return new ResponseMeta<MicroserviceBO>(ErrorCode.SUCCESS, microservice.queryServiceDetail(serviceId));
    }

    @GetMapping("/services/{serviceId}/statics")
    public ResponseMeta<ServiceStaticsDto> queryServiceStatics(@PathVariable("serviceId") String serviceId) {
        return new ResponseMeta<ServiceStaticsDto>(ErrorCode.SUCCESS, microservice.getServiceStatics(serviceId));
    }

    @GetMapping("/services/{serviceId}/members")
    public ResponseMeta<List<UserBO>> queryServiceMembers(@PathVariable("serviceId") String serviceId) {
        return new ResponseMeta<List<UserBO>>(ErrorCode.SUCCESS, microservice.queryServiceMembers(serviceId));
    }

    @PostMapping("/services/{serviceId}/members")
    public ResponseMeta<Boolean> addServiceMember(@RequestBody ServiceMemberDto serviceMemberDto) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, microservice.addServiceMember(serviceMemberDto));
    }

    @DeleteMapping("/services/{serviceId}/members/{userId}")
    public ResponseMeta<Boolean> deleteServiceMember(@PathVariable("serviceId") String serviceId,
                                                  @PathVariable("userId") String userId) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, microservice.deleteServiceMember(serviceId, userId));
    }

    @ResponseBody
    @PostMapping("/services")
    public ResponseMeta<String> createService(@RequestBody ServiceDto serviceDto) {
        return new ResponseMeta<String>(ErrorCode.SUCCESS, microservice.createService(serviceDto));
    }

    @ResponseBody
    @PutMapping("/services")
    public ResponseMeta<String> updateService(@RequestBody ServiceDto update) {
        return new ResponseMeta<String>(ErrorCode.SUCCESS, microservice.updateService(update));
    }

    @ResponseBody
    @DeleteMapping("/service/{serviceId}")
    public ResponseMeta<Boolean> deleteService(@PathVariable("serviceId") String serviceId) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, microservice.deleteService(serviceId));
    }

    @GetMapping(value = "/system/build/versions")
    public ResponseMeta<List<BuildToolBO>> getToolVersions() {
        return new ResponseMeta<>(ErrorCode.SUCCESS, microservice.getToolVersions());
    }
    @ResponseBody
    @PostMapping("/system/builds")
    public ResponseMeta<Boolean> createBuildTool(@Validated @RequestBody SystemBuildDto systemBuildDto) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, microservice.createBuildTool(systemBuildDto));
    }

    @ResponseBody
    @DeleteMapping("/system/builds/{toolId}")
    public ResponseMeta<Boolean> deleteBuildTool(@PathVariable("toolId") String toolId) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS, microservice.deleteBuildTool(toolId));
    }
}
