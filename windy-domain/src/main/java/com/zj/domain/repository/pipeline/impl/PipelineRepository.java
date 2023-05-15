package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.PipelineDTO;
import com.zj.domain.entity.po.pipeline.Pipeline;
import com.zj.domain.mapper.pipeline.PipelineMapper;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author falcon
 * @since 2023/5/15
 */
@Repository
public class PipelineRepository extends ServiceImpl<PipelineMapper, Pipeline> implements
    IPipelineRepository {

  public boolean updatePipeline(PipelineDTO pipelineDTO) {
    Long currentTime = System.currentTimeMillis();
    Pipeline pipeline = OrikaUtil.convert(pipelineDTO, Pipeline.class);
    pipeline.setUpdateTime(currentTime);
    return update(pipeline, Wrappers.lambdaUpdate(Pipeline.class)
        .eq(Pipeline::getPipelineId, pipeline.getPipelineId()));
  }

  public boolean createPipeline(PipelineDTO pipelineDTO) {
    Long currentTime = System.currentTimeMillis();
    Pipeline pipeline = OrikaUtil.convert(pipelineDTO, Pipeline.class);
    pipeline.setPipelineId(pipeline.getPipelineId());
    pipeline.setPipelineStatus(pipelineDTO.getPipelineStatus());
    pipeline.setUpdateTime(currentTime);
    pipeline.setCreateTime(currentTime);
    return save(pipeline);
  }

  public PipelineDTO getPipeline(String pipelineId) {
    Pipeline pipeline = getOne(
        Wrappers.<Pipeline>lambdaQuery().eq(Pipeline::getPipelineId, pipelineId));
    return OrikaUtil.convert(pipeline, PipelineDTO.class);
  }

  @Override
  public boolean deletePipeline(String pipelineId) {
    return remove(Wrappers.lambdaQuery(Pipeline.class).eq(Pipeline::getPipelineId, pipelineId));
  }

  @Override
  public List<PipelineDTO> listPipelines(String serviceId) {
    Assert.notEmpty(serviceId, "service can not be empty");

    List<Pipeline> pipelines = list(
        Wrappers.lambdaQuery(Pipeline.class).eq(Pipeline::getServiceId, serviceId));

    if (CollectionUtils.isEmpty(pipelines)) {
      return new ArrayList<>();
    }

    return pipelines.stream().map(pipeline -> OrikaUtil.convert(pipeline, PipelineDTO.class))
        .collect(Collectors.toList());
  }
}
