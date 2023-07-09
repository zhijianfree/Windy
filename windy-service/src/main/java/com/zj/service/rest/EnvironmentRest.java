package com.zj.service.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.model.PageSize;
import com.zj.common.model.ResponseMeta;
import com.zj.domain.entity.dto.service.DeployEnvironmentDto;
import com.zj.service.service.EnvironmentService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/devops")
public class EnvironmentRest {

  private EnvironmentService environmentService;

  public EnvironmentRest(EnvironmentService environmentService) {
    this.environmentService = environmentService;
  }

  @GetMapping("/environments")
  public ResponseMeta<PageSize<DeployEnvironmentDto>> getAllEnvironments(@RequestParam("page") Integer page,
      @RequestParam("size") Integer size) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.getEnvironments(page, size));
  }

  @PostMapping("/environments")
  public ResponseMeta<Boolean> createEnvironment(@RequestBody DeployEnvironmentDto deployEnvironment) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.createEnvironment(deployEnvironment));
  }

  @PutMapping("/environment")
  public ResponseMeta<Boolean> updateEnvironment(@RequestBody DeployEnvironmentDto deployEnvironment) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.updateEnvironment(deployEnvironment));
  }

  @DeleteMapping("/environments/{envId}")
  public ResponseMeta<Boolean> deleteEnvironment(@PathVariable("envId") String envId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.deleteEnvironment(envId));
  }

  @GetMapping("/environments/{envId}")
  public ResponseMeta<DeployEnvironmentDto> getEnvironment(@PathVariable("envId") String envId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.getEnvironment(envId));
  }
}
