package com.zj.pipeline.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.pipeline.SystemConfigDto;
import com.zj.pipeline.service.SystemConfigService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/v1/devops/pipeline")
@RestController
public class SystemConfigRest {

    private SystemConfigService systemConfigService;

    public SystemConfigRest(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

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
    public ResponseMeta<Boolean> updateSystemConfig( @RequestBody SystemConfigDto systemConfigDto) {
        return new ResponseMeta<Boolean>(ErrorCode.SUCCESS,
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

    @ResponseBody
    @GetMapping("/system/monitor")
    public ResponseMeta<Object> getMonitorInfo() {
        return new ResponseMeta<Object>(ErrorCode.SUCCESS,
            systemConfigService.getSystemMonitor());
    }
}
