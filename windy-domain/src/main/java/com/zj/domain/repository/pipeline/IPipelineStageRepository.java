package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PipelineStageBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface IPipelineStageRepository {

  /**
   * 删除流水线阶段
   *
   * @param stageIds 阶段ID列表
   * @return 删除是否成功
   */
  boolean deletePipelineStages(List<String> stageIds);

  /**
   * 获取流水线阶段
   * @param stageId 阶段ID
   * @return 阶段信息
   */
  PipelineStageBO getPipelineStage(String stageId);

  /**
   * 更新流水线阶段
   * @param stageDto 阶段信息
   * @return 更新是否成功
   */
  boolean updateStage(PipelineStageBO stageDto);

  /**
   * 删除流水线所有阶段
   * @param pipelineId 流水线ID
   * @return 删除是否成功
   */
  boolean deleteStagesByPipelineId(String pipelineId);

  /**
   * 保存流水线阶段
   * @param pipelineStage 阶段信息
   */
  void saveStage(PipelineStageBO pipelineStage);

    /**
     * 排序流水线节点
     * @param pipelineId 流水线ID
     * @return 节点列表
     */
  List<PipelineStageBO> sortPipelineNodes(String pipelineId);
}
