package com.zj.master.dispatch.pipeline.listener;

import com.zj.domain.entity.dto.pipeline.CodeChangeDto;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.dto.pipeline.PublishBindDto;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.repository.pipeline.ICodeChangeRepository;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.IPublishBindRepository;
import com.zj.master.entity.vo.NodeStatusChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 流水线成功过完成之后删除发布绑定的分支
 * @author guyuelan
 * @since 2023/6/29
 */
@Slf4j
@Component
public class PublishRemoveListener implements IPipelineEndListener {

  private final IPublishBindRepository publishBindRepository;
  private final IPipelineRepository pipelineRepository;
  private final ICodeChangeRepository codeChangeRepository;

  public PublishRemoveListener(IPublishBindRepository publishBindRepository,
                               IPipelineRepository pipelineRepository, ICodeChangeRepository codeChangeRepository) {
    this.publishBindRepository = publishBindRepository;
    this.pipelineRepository = pipelineRepository;
    this.codeChangeRepository = codeChangeRepository;
  }

  @Override
  public void handleEnd(NodeStatusChange statusChange) {
    PipelineDto pipeline = pipelineRepository.getPipeline(statusChange.getPipelineId());
    if (Objects.isNull(pipeline)) {
      return;
    }

    if (Objects.equals(pipeline.getPipelineType(), PipelineType.PUBLISH.getType())
        && statusChange.getProcessStatus().isSuccess()) {
      List<PublishBindDto> pipelinePublishes = publishBindRepository.getPipelinePublishes(pipeline.getPipelineId());
      List<String> branches = pipelinePublishes.stream().map(PublishBindDto::getBranch).collect(Collectors.toList());
      List<String> serviceChanges = codeChangeRepository.getServiceChanges(pipeline.getServiceId())
              .stream().filter(codeChange -> branches.contains(codeChange.getChangeBranch()))
              .map(CodeChangeDto::getChangeId).collect(Collectors.toList());
      boolean batchDeleteCodeChange = codeChangeRepository.batchDeleteCodeChange(serviceChanges);
      boolean deletePublishLine = publishBindRepository.deletePipelinePublishes(statusChange.getPipelineId());
      log.info("delete code change result = {} delete publish rsult={}", batchDeleteCodeChange, deletePublishLine);
    }
  }
}
