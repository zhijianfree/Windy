package com.zj.master.dispatch.pipeline.intercept;

import com.zj.domain.entity.dto.service.MicroserviceDto;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.master.entity.vo.TaskNode;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/6/20
 */
@Component
public class PresetInterceptor implements INodeExecuteInterceptor {
  private IMicroServiceRepository microServiceRepository;

  public PresetInterceptor(IMicroServiceRepository microServiceRepository) {
    this.microServiceRepository = microServiceRepository;
  }

  @Override
  public void beforeExecute(TaskNode taskNode) {
    MicroserviceDto service = microServiceRepository.queryServiceDetail(taskNode.getServiceId());
    if (Objects.isNull(service)) {
      return;
    }

    taskNode.getRequestContext().setGitUrl(service.getGitUrl());
  }
}
