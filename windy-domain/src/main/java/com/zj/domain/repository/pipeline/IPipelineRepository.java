package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PipelineBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineRepository {

  /**
   * 更新流水线
   * @param pipelineBO 流水线信息
   * @return 是否成功
   */
  boolean updatePipeline(PipelineBO pipelineBO);

  /**
   * 创建流水线
   * @param pipelineBO 流水线信息
   * @return 是否成功
   */
  boolean createPipeline(PipelineBO pipelineBO);

  /**
   * 获取流水线
   * @param pipelineId 流水线ID
   * @return 流水线信息
   */
  PipelineBO getPipeline(String pipelineId);

  /**
   * 删除流水线
   * @param pipelineId 流水线ID
   * @return 是否成功
   */
  boolean deletePipeline(String pipelineId);

  /**
   * 获取服务流水线
   * @param serviceId 服务ID
   * @return 流水线列表
   */
  List<PipelineBO> getServicePipelines(String serviceId);

  /**
   * 获取发布类型流水线
   * @param serviceId 服务ID
   * @return 流水线信息
   */
  PipelineBO getPublishPipeline(String serviceId);

  /**
   * 获取定时流水线
   * @return 流水线列表
   */
  List<PipelineBO> getSchedulePipelines();
}
