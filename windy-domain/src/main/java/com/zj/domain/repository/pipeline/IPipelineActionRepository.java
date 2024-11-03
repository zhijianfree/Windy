package com.zj.domain.repository.pipeline;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineActionRepository {

  Boolean createAction(PipelineActionBO actionDto);

  PipelineActionBO getAction(String actionId);

  Boolean updateAction(PipelineActionBO actionDto);

  Boolean actionsBindNode(String nodeId, List<String> actionIds);

  Boolean deleteAction(String actionId);

  PageSize<PipelineActionBO> getActions(Integer page, Integer size, String name);

  List<PipelineActionBO> getActionsByNodeId(String nodeId);

  boolean batchDelete(List<String> removeList);
}
