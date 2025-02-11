package com.zj.service.rest;

import com.zj.common.exception.ErrorCode;
import com.zj.common.entity.dto.PageSize;
import com.zj.common.entity.dto.ResponseMeta;
import com.zj.domain.entity.bo.service.DeployEnvironmentBO;
import com.zj.service.entity.NodeInfo;
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

  private final EnvironmentService environmentService;

  public EnvironmentRest(EnvironmentService environmentService) {
    this.environmentService = environmentService;
  }

  @GetMapping("/environments")
  public ResponseMeta<PageSize<DeployEnvironmentBO>> getEnvironments(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                                     @RequestParam(value = "size", defaultValue = "10") Integer size, @RequestParam(value = "name", defaultValue = "") String name) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.getEnvironments(page, size, name));
  }

  @GetMapping("/environments/all")
  public ResponseMeta<List<DeployEnvironmentBO>> getAllEnvironments() {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.getAvailableEnvs());
  }

  @PostMapping("/environments")
  public ResponseMeta<Boolean> createEnvironment(@RequestBody DeployEnvironmentBO deployEnvironment) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.createEnvironment(deployEnvironment));
  }

  @PutMapping("/environment")
  public ResponseMeta<Boolean> updateEnvironment(@RequestBody DeployEnvironmentBO deployEnvironment) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.updateEnvironment(deployEnvironment));
  }

  @DeleteMapping("/environments/{envId}")
  public ResponseMeta<Boolean> deleteEnvironment(@PathVariable("envId") String envId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.deleteEnvironment(envId));
  }

  @GetMapping("/environments/{envId}")
  public ResponseMeta<DeployEnvironmentBO> getEnvironment(@PathVariable("envId") String envId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.getEnvironment(envId));
  }

  @PostMapping("/environment/check")
  public ResponseMeta<Boolean> checkStatus(@RequestBody String data, @RequestParam("checkType") Integer checkType) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.checkStatus(checkType, data));
  }

  @GetMapping("/environment/{envId}/nodes")
  public ResponseMeta<List<NodeInfo>> getNodeList(@PathVariable("envId") String envId) {
    return new ResponseMeta<>(ErrorCode.SUCCESS, environmentService.getNodeList(envId));
  }
}
