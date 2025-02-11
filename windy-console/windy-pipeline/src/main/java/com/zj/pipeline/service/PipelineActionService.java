package com.zj.pipeline.service;

import com.zj.common.entity.dto.PageSize;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;
import com.zj.domain.repository.pipeline.IPipelineActionRepository;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/3/27
 */
@Service
public class PipelineActionService {

  private final UniqueIdService uniqueIdService;
  private final IPipelineActionRepository pipelineActionRepository;

  public PipelineActionService(UniqueIdService uniqueIdService,
      IPipelineActionRepository pipelineActionRepository) {
    this.uniqueIdService = uniqueIdService;
    this.pipelineActionRepository = pipelineActionRepository;
  }

  public Boolean createAction(PipelineActionBO actionDto) {
    actionDto.setActionId(uniqueIdService.getUniqueId());
    return pipelineActionRepository.createAction(actionDto);
  }

  public PipelineActionBO getAction(String actionId) {
    return pipelineActionRepository.getAction(actionId);
  }

  public Boolean updateAction(PipelineActionBO actionDto) {
    return pipelineActionRepository.updateAction(actionDto);
  }

  public Boolean actionsBindNode(String nodeId, List<String> actionIds) {
    return pipelineActionRepository.actionsBindNode(nodeId, actionIds);
  }

  public Boolean deleteAction(String actionId) {
    return pipelineActionRepository.deleteAction(actionId);
  }

  public PageSize<PipelineActionBO> getActions(Integer page, Integer size, String name) {
    return pipelineActionRepository.getActions(page, size, name);
  }

  public List<PipelineActionBO> getActionsByNodeId(String nodeId) {
    return pipelineActionRepository.getActionsByNodeId(nodeId);
  }

  public boolean batchDelete(List<String> removeList) {
    return pipelineActionRepository.batchDelete(removeList);
  }
}
