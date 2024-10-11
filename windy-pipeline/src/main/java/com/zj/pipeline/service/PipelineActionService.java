package com.zj.pipeline.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.model.PageSize;
import com.zj.common.uuid.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.ActionParam;
import com.zj.domain.entity.dto.pipeline.CompareResult;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.domain.entity.po.pipeline.PipelineAction;
import com.zj.domain.repository.pipeline.IPipelineActionRepository;
import java.util.List;
import java.util.stream.Collectors;
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

  public Boolean createAction(PipelineActionDto actionDto) {
    actionDto.setActionId(uniqueIdService.getUniqueId());
    return pipelineActionRepository.createAction(actionDto);
  }

  public PipelineActionDto getAction(String actionId) {
    return pipelineActionRepository.getAction(actionId);
  }

  public Boolean updateAction(PipelineActionDto actionDto) {
    return pipelineActionRepository.updateAction(actionDto);
  }

  public Boolean actionsBindNode(String nodeId, List<String> actionIds) {
    return pipelineActionRepository.actionsBindNode(nodeId, actionIds);
  }

  public Boolean deleteAction(String actionId) {
    return pipelineActionRepository.deleteAction(actionId);
  }

  public PageSize<PipelineActionDto> getActions(Integer page, Integer size, String name) {
    return pipelineActionRepository.getActions(page, size, name);
  }

  public List<PipelineActionDto> getActionsByNodeId(String nodeId) {
    return pipelineActionRepository.getActionsByNodeId(nodeId);
  }

  public boolean batchDelete(List<String> removeList) {
    return pipelineActionRepository.batchDelete(removeList);
  }
}
