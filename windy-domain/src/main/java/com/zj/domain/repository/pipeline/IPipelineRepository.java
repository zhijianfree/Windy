package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.bo.pipeline.PipelineBO;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineRepository {

  /**
   * 修改流水线
   * */
  boolean updatePipeline(PipelineBO pipelineBO);

  /**
   * 创建流水线
   * */
  boolean createPipeline(PipelineBO pipelineBO);

  /**
   * 获取流水线
   * */
  PipelineBO getPipeline(String pipelineId);

  /**
   * 删除流水线
   * */
  boolean deletePipeline(String pipelineId);

  /**
   * 获取服务流水线列表
   * */
  List<PipelineBO> listPipelines(String serviceId);

  List<PipelineBO> getServicePipelines(String serviceId);

  PipelineBO getPublishPipeline(String serviceId);

  List<PipelineBO> getSchedulePipelines();

  Integer countAll();
}
