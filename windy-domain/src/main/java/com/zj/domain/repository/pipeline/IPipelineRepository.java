package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.dto.pipeline.PipelineDTO;
import java.util.List;

/**
 * @author falcon
 * @since 2023/5/15
 */
public interface IPipelineRepository {

  /**
   * 修改流水线
   * */
  boolean updatePipeline(PipelineDTO pipelineDTO);

  /**
   * 创建流水线
   * */
  boolean createPipeline(PipelineDTO pipelineDTO);

  /**
   * 获取流水线
   * */
  PipelineDTO getPipeline(String pipelineId);

  /**
   * 删除流水线
   * */
  boolean deletePipeline(String pipelineId);

  /**
   * 获取服务流水线列表
   * */
  List<PipelineDTO> listPipelines(String serviceId);
}
