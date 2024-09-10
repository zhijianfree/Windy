package com.zj.pipeline.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.git.GitAccessInfo;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.pipeline.SystemConfigDto;
import com.zj.domain.entity.vo.DefaultPipelineVo;
import com.zj.domain.entity.vo.ImageRepositoryVo;
import com.zj.domain.entity.vo.MavenConfigVo;
import com.zj.pipeline.service.SystemConfigService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/v1/devops/pipeline")
@RestController
public class SystemConfigRest {

  private final SystemConfigService systemConfigService;

  public SystemConfigRest(SystemConfigService systemConfigService) {
    this.systemConfigService = systemConfigService;
  }

  @ResponseBody
  @GetMapping("/system/configs")
  public ResponseMeta<List<SystemConfigDto>> listSystemConfigs() {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.listSystemConfigs());
  }

  @ResponseBody
  @PostMapping("/system/configs")
  public ResponseMeta<String> createSystemConfig(@RequestBody SystemConfigDto config) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.createSystemConfig(config));
  }

  @ResponseBody
  @PutMapping("/system/config")
  public ResponseMeta<Boolean> updateSystemConfig(@RequestBody SystemConfigDto config) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.updateSystemConfig(config));
  }

  @ResponseBody
  @DeleteMapping("/system/config/{configId}")
  public ResponseMeta<Boolean> deleteSystemConfig(@PathVariable("configId") String configId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.deleteSystemConfig(configId));
  }

  @ResponseBody
  @GetMapping("/system/config/{configId}")
  public ResponseMeta<SystemConfigDto> getSystemConfig(@PathVariable("configId") String configId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.getSystemConfig(configId));
  }

  @ResponseBody
  @GetMapping("/system/config/git")
  public ResponseMeta<GitAccessInfo> getGitConfig() {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.getGitConfig());
  }

  @ResponseBody
  @PutMapping("/system/config/git")
  public ResponseMeta<Boolean> updateGit(@RequestBody GitAccessInfo config) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.updateGitConfig(config));
  }

  @ResponseBody
  @PutMapping("/system/config/maven")
  public ResponseMeta<Boolean> updateMaven(@RequestBody MavenConfigVo mavenConfig) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.updateMavenConfig(mavenConfig));
  }

  @ResponseBody
  @GetMapping("/system/config/maven")
  public ResponseMeta<MavenConfigVo> getMavenConfig() {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.getMavenConfig());
  }

  @ResponseBody
  @GetMapping("/system/config/repository")
  public ResponseMeta<ImageRepositoryVo> getRepositoryConfig() {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.getImageRepository());
  }

  @ResponseBody
  @PutMapping("/system/config/repository")
  public ResponseMeta<Boolean> updateRepository(@RequestBody ImageRepositoryVo repository) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.updateRepository(repository));
  }

  @ResponseBody
  @GetMapping("/system/config/pipe")
  public ResponseMeta<DefaultPipelineVo> getDefaultPipeline() {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.getDefaultPipeline());
  }

  @ResponseBody
  @GetMapping("/system/monitor")
  public ResponseMeta<Object> getMonitorInfo() {
    return new ResponseMeta<>(ErrorCode.SUCCESS, systemConfigService.getSystemMonitor());
  }
}
