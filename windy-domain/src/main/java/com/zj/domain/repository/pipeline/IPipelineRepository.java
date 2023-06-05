package com.zj.domain.repository.pipeline;

import com.zj.domain.entity.dto.pipeline.PipelineDto;
import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
public interface IPipelineRepository {

  /**
   * 修改流水线
   * */
  boolean updatePipeline(PipelineDto pipelineDTO);

  /**
   * 创建流水线
   * */
  boolean createPipeline(PipelineDto pipelineDTO);

  /**
   * 获取流水线
   * */
  PipelineDto getPipeline(String pipelineId);

  /**
   * 删除流水线
   * */
  boolean deletePipeline(String pipelineId);

  /**
   * 获取服务流水线列表
   * */
  List<PipelineDto> listPipelines(String serviceId);

  List<PipelineDto> getServicePipelines(String serviceId);
}
