package com.zj.domain.repository.pipeline;

import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineActionRepository {

  /**
   * 创建流水线动作
   * @param actionDto 动作信息
   * @return 是否成功
   */
  Boolean createAction(PipelineActionBO actionDto);

  /**
   * 获取流水线动作
   * @param actionId 动作ID
   * @return 动作信息
   */
  PipelineActionBO getAction(String actionId);

  /**
   * 更新流水线动作
   * @param actionDto 动作信息
   * @return 是否成功
   */
  Boolean updateAction(PipelineActionBO actionDto);

  /**
   * 绑定动作节点
   * @param nodeId 节点ID
   * @param actionIds 动作ID列表
   * @return 是否成功
   */
  Boolean actionsBindNode(String nodeId, List<String> actionIds);

  /**
   * 删除流水线动作
   * @param actionId 动作ID
   * @return 是否成功
   */
  Boolean deleteAction(String actionId);

  /**
   * 分页获取流水线动作
   * @param page 页码
   * @param size 每页数量
   * @param name 动作名称
   * @return 动作列表
   */
  PageSize<PipelineActionBO> getActions(Integer page, Integer size, String name);

  /**
   * 获取节点动作
   * @param nodeId 节点ID
   * @return 动作列表
   */
  List<PipelineActionBO> getActionsByNodeId(String nodeId);

    /**
     * 批量删除动作
     * @param actionIds 动作ID列表
     * @return 是否成功
     */
  boolean batchDelete(List<String> actionIds);
}
