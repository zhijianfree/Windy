package com.zj.pipeline.rest;

import com.zj.common.ResponseMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.pipeline.entity.dto.MicroserviceDto;
import com.zj.pipeline.service.MicroserviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1/devops")
@RestController
public class MicroserviceRest {

    @Autowired
    private MicroserviceService microservice;

    @ResponseBody
    @GetMapping("/services")
    public ResponseMeta<List<MicroserviceDto>> queryServices() {
        return new ResponseMeta<List<MicroserviceDto>>(ErrorCode.SUCCESS, microservice.getServices());
    }

    @ResponseBody
    @PostMapping("/services")
    public ResponseMeta<String> createService(@RequestBody MicroserviceDto microserviceDto) {
        return new ResponseMeta<String>(ErrorCode.SUCCESS, microservice.createService(microserviceDto));
    }
}
