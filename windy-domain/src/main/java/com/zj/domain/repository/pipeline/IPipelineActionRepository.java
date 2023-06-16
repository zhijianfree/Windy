package com.zj.domain.repository.pipeline;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.domain.entity.po.pipeline.PipelineAction;
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

  IPage<PipelineAction>  getActions(Integer page, Integer size, String name);

  List<PipelineAction> getActionsByNodeId(String nodeId);

  boolean batchDelete(List<String> removeList);
}
