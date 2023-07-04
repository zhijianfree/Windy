package com.zj.master.dispatch.pipeline.listener;

import com.zj.common.enums.ProcessStatus;
import com.zj.domain.entity.dto.pipeline.PipelineDto;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import com.zj.domain.repository.pipeline.IPublishBindRepository;
import com.zj.master.entity.vo.NodeStatusChange;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/6/29
 */
@Component
public class PublishRemoveListener implements IPipelineEndListener {

  private IPublishBindRepository publishBindRepository;
  private IPipelineRepository pipelineRepository;

  public PublishRemoveListener(IPublishBindRepository publishBindRepository,
      IPipelineRepository pipelineRepository) {
    this.publishBindRepository = publishBindRepository;
    this.pipelineRepository = pipelineRepository;
  }

  @Override
  public void handleEnd(NodeStatusChange statusChange) {
    PipelineDto pipeline = pipelineRepository.getPipeline(statusChange.getPipelineId());
    if (Objects.isNull(pipeline)) {
      return;
    }

    if (Objects.equals(pipeline.getPipelineType(), PipelineType.PUBLISH.getType())
        && statusChange.getProcessStatus().isSuccess()) {
      publishBindRepository.deletePublishLine(statusChange.getPipelineId());
    }
  }
}
