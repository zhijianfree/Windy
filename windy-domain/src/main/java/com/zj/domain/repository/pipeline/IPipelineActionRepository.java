package com.zj.domain.repository.pipeline;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.pipeline.PipelineActionDto;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineActionRepository {

  Boolean createAction(PipelineActionDto actionDto);

  PipelineActionDto getAction(String actionId);

  Boolean updateAction(PipelineActionDto actionDto);

  Boolean actionsBindNode(String nodeId, List<String> actionIds);

  Boolean deleteAction(String actionId);

  PageSize<PipelineActionDto> getActions(Integer page, Integer size, String name);

  List<PipelineActionDto> getActionsByNodeId(String nodeId);

  boolean batchDelete(List<String> removeList);
}
