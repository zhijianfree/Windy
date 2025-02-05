package com.zj.master.dispatch.pipeline.intercept;

import com.zj.common.enums.ExecuteType;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.bo.pipeline.PipelineNodeBO;
import com.zj.domain.entity.bo.pipeline.PublishBindBO;
import com.zj.domain.entity.bo.service.MicroserviceBO;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.IPublishBindRepository;
import com.zj.domain.repository.service.IMicroServiceRepository;
import com.zj.master.entity.vo.MergeMasterContext;
import com.zj.common.entity.pipeline.PipelineConfig;
import com.zj.master.entity.vo.TaskNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 只处理代码合并的节点
 * @author guyuelan
 * @since 2023/6/29
 */
@Slf4j
@Component
public class MergeNodeInterceptor implements INodeExecuteInterceptor{
  public static final String TAG_NAME = "tagName";
  private final IPipelineNodeRepository pipelineNodeRepository;
  private final IPublishBindRepository publishBindRepository;
  private final IPipelineRepository pipelineRepository;
  private final IMicroServiceRepository microServiceRepository;

  public MergeNodeInterceptor(IPipelineNodeRepository pipelineNodeRepository,
                              IPublishBindRepository publishBindRepository, IPipelineRepository pipelineRepository,
                              IMicroServiceRepository microServiceRepository) {
    this.pipelineNodeRepository = pipelineNodeRepository;
    this.publishBindRepository = publishBindRepository;
    this.pipelineRepository = pipelineRepository;
    this.microServiceRepository = microServiceRepository;
  }

  @Override
  public int sort() {
    return 3;
  }

  @Override
  public void beforeExecute(TaskNode taskNode) {
    if (!Objects.equals(taskNode.getExecuteType(), ExecuteType.MERGE.name())) {
      return;
    }

    PipelineNodeBO pipelineNode = pipelineNodeRepository.getPipelineNode(taskNode.getNodeId());
    PipelineBO pipeline = pipelineRepository.getPipeline(pipelineNode.getPipelineId());
    List<PublishBindBO> servicePublishes = publishBindRepository.getServicePublishes(pipeline.getServiceId());
    List<String> branches = servicePublishes.stream().map(PublishBindBO::getBranch).collect(Collectors.toList());
    String messages = servicePublishes.stream().map(PublishBindBO::getMessage)
            .filter(StringUtils::isNotBlank).collect(Collectors.joining("\n"));
    MergeMasterContext requestContext = (MergeMasterContext) taskNode.getRequestContext();
    PipelineConfig pipelineConfig = pipeline.getPipelineConfig();
    if (Objects.nonNull(pipelineConfig) && MapUtils.isNotEmpty(pipelineConfig.getParamList())) {
      Object tagName = pipelineConfig.getParamList().get(TAG_NAME);
      requestContext.setTagName(String.valueOf(tagName));
    }
    MicroserviceBO service = microServiceRepository.queryServiceDetail(pipeline.getServiceId());
    requestContext.setMainBranch(service.getServiceConfig().getServiceContext().getMainBranch());
    requestContext.setBranches(branches);
    requestContext.setMessage(messages);
    taskNode.setRequestContext(requestContext);
  }
}
