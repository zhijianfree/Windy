package com.zj.master.dispatch.pipeline.intercept;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.zj.common.adapter.git.GitAccessInfo;
import com.zj.common.entity.pipeline.ServiceConfig;
import com.zj.common.enums.GitType;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.repository.pipeline.ISystemConfigRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.master.entity.vo.TaskNode;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * 在流水线每个节点执行都会处理，用来添加全局的参数配置
 * @author guyuelan
 * @since 2023/6/20
 */
@Component
public class PresetInterceptor implements INodeExecuteInterceptor {
  private final IMicroServiceRepository microServiceRepository;

  private final ISystemConfigRepository systemConfigRepository;

  public PresetInterceptor(IMicroServiceRepository microServiceRepository,
      ISystemConfigRepository systemConfigRepository) {
    this.microServiceRepository = microServiceRepository;
    this.systemConfigRepository = systemConfigRepository;
  }

  @Override
  public int sort() {
    return 1;
  }

  @Override
  public void beforeExecute(TaskNode taskNode) {
    MicroserviceBO service = microServiceRepository.queryServiceDetail(taskNode.getServiceId());
    if (Objects.isNull(service)) {
      return;
    }

    taskNode.getRequestContext().setGitUrl(service.getGitUrl());
    GitAccessInfo gitAccess = Optional.ofNullable(service.getServiceConfig())
            .map(ServiceConfig::getGitAccessInfo).filter(access -> StringUtils.isNotBlank(access.getAccessToken()))
            .orElseGet(systemConfigRepository::getGitAccess);
    GitType gitType = GitType.exchange(gitAccess.getGitType());
    taskNode.getRequestContext().setGitType(gitType.name());
    taskNode.getRequestContext().setTokenName(gitAccess.getOwner());
    taskNode.getRequestContext().setToken(gitAccess.getAccessToken());
  }
}
