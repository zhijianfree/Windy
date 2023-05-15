package com.zj.pipeline.rest;

import com.zj.common.ResponseMeta;
import com.zj.domain.entity.dto.pipeline.SystemConfigDto;
import com.zj.common.exception.ErrorCode;
import com.zj.pipeline.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1/devops/pipeline")
@RestController
public class SystemConfigRest {

    @Autowired
    private SystemConfigService systemConfigService;

    @ResponseBody
    @GetMapping("/system/configs")
    public ResponseMeta<List<SystemConfigDto>> listSystemConfigs() {
        return new ResponseMeta<List<SystemConfigDto>>(ErrorCode.SUCCESS,
                systemConfigService.listSystemConfigs());
    }

    @ResponseBody
    @PostMapping("/system/configs")
    public ResponseMeta<String> createSystemConfig(@RequestBody SystemConfigDto systemConfigDto) {
        return new ResponseMeta<String>(ErrorCode.SUCCESS,
                systemConfigService.createSystemConfig(systemConfigDto));
    }

    @ResponseBody
    @PutMapping("/system/config")
    public ResponseMeta<String> updateSystemConfig( @RequestBody SystemConfigDto systemConfigDto) {
        return new ResponseMeta<String>(ErrorCode.SUCCESS,
                systemConfigService.updateSystemConfig(systemConfigDto));
    }

    @ResponseBody
    @DeleteMapping("/system/config/{configId}")
    public ResponseMeta<Boolean> deleteSystemConfig( @PathVariable("configId") String configId) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS,
                systemConfigService.deleteSystemConfig(configId));
    }

    @ResponseBody
    @GetMapping("/system/config/{configId}")
    public ResponseMeta<SystemConfigDto> getSystemConfig( @PathVariable("configId") String configId) {
        return new ResponseMeta<SystemConfigDto>(ErrorCode.SUCCESS,
                systemConfigService.getSystemConfig(configId));
    }
}
