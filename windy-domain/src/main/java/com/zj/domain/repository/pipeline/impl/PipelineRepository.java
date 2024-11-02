package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.PipelineDto;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.entity.po.pipeline.Pipeline;
import com.zj.domain.mapper.pipeline.PipelineMapper;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Repository
public class PipelineRepository extends ServiceImpl<PipelineMapper, Pipeline> implements
    IPipelineRepository {

  public boolean updatePipeline(PipelineDto pipelineDTO) {
    Long currentTime = System.currentTimeMillis();
    Pipeline pipeline = OrikaUtil.convert(pipelineDTO, Pipeline.class);
    pipeline.setUpdateTime(currentTime);
    return update(pipeline, Wrappers.lambdaUpdate(Pipeline.class)
        .eq(Pipeline::getPipelineId, pipeline.getPipelineId()));
  }

  public boolean createPipeline(PipelineDto pipelineDTO) {
    Long currentTime = System.currentTimeMillis();
    Pipeline pipeline = OrikaUtil.convert(pipelineDTO, Pipeline.class);
    pipeline.setPipelineId(pipeline.getPipelineId());
    pipeline.setPipelineStatus(pipelineDTO.getPipelineStatus());
    pipeline.setUpdateTime(currentTime);
    pipeline.setCreateTime(currentTime);
    return save(pipeline);
  }

  public PipelineDto getPipeline(String pipelineId) {
    Pipeline pipeline = getOne(
        Wrappers.<Pipeline>lambdaQuery().eq(Pipeline::getPipelineId, pipelineId));
    return OrikaUtil.convert(pipeline, PipelineDto.class);
  }

  @Override
  public boolean deletePipeline(String pipelineId) {
    return remove(Wrappers.lambdaQuery(Pipeline.class).eq(Pipeline::getPipelineId, pipelineId));
  }

  @Override
  public List<PipelineDto> listPipelines(String serviceId) {
    Assert.notEmpty(serviceId, "service can not be empty");

    List<Pipeline> pipelines = list(
        Wrappers.lambdaQuery(Pipeline.class).eq(Pipeline::getServiceId, serviceId));

    if (CollectionUtils.isEmpty(pipelines)) {
      return new ArrayList<>();
    }

    return pipelines.stream().map(pipeline -> OrikaUtil.convert(pipeline, PipelineDto.class))
        .collect(Collectors.toList());
  }

  @Override
  public List<PipelineDto> getServicePipelines(String serviceId) {
    List<Pipeline> pipelines = list(
        Wrappers.<Pipeline>lambdaQuery().eq(Pipeline::getServiceId, serviceId));
    return OrikaUtil.convertList(pipelines, PipelineDto.class);
  }

  @Override
  public PipelineDto getPublishPipeline(String serviceId) {
    Pipeline pipeline = getOne(Wrappers.<Pipeline>lambdaQuery().eq(Pipeline::getServiceId, serviceId)
        .eq(Pipeline::getPipelineType, PipelineType.PUBLISH.getType()));
    return OrikaUtil.convert(pipeline, PipelineDto.class);
  }

  @Override
  public List<PipelineDto> getSchedulePipelines() {
    List<Pipeline> pipelines = list(Wrappers.<Pipeline>lambdaQuery()
        .eq(Pipeline::getPipelineType, PipelineType.SCHEDULE.getType()));
    return OrikaUtil.convertList(pipelines, PipelineDto.class);
  }

  @Override
  public Integer countAll() {
    return count();
  }
}
