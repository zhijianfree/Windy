package com.zj.domain.repository.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.pipeline.PipelineConfig;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.PipelineBO;
import com.zj.domain.entity.enums.PipelineType;
import com.zj.domain.entity.po.pipeline.Pipeline;
import com.zj.domain.mapper.pipeline.PipelineMapper;
import com.zj.domain.repository.pipeline.IPipelineRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

  public boolean updatePipeline(PipelineBO pipelineBO) {
    Long currentTime = System.currentTimeMillis();
    Pipeline pipeline = convertPipeline(pipelineBO);
    pipeline.setUpdateTime(currentTime);
    return update(pipeline, Wrappers.lambdaUpdate(Pipeline.class)
        .eq(Pipeline::getPipelineId, pipeline.getPipelineId()));
  }

  public boolean createPipeline(PipelineBO pipelineBO) {
    Long currentTime = System.currentTimeMillis();
    Pipeline pipeline = convertPipeline(pipelineBO);
    pipeline.setPipelineId(pipeline.getPipelineId());
    pipeline.setPipelineStatus(pipelineBO.getPipelineStatus());
    pipeline.setUpdateTime(currentTime);
    pipeline.setCreateTime(currentTime);
    return save(pipeline);
  }

  public PipelineBO getPipeline(String pipelineId) {
    Pipeline pipeline = getOne(
        Wrappers.<Pipeline>lambdaQuery().eq(Pipeline::getPipelineId, pipelineId));
    return convertPipelineBO(pipeline);
  }

  @Override
  public boolean deletePipeline(String pipelineId) {
    return remove(Wrappers.lambdaQuery(Pipeline.class).eq(Pipeline::getPipelineId, pipelineId));
  }

  @Override
  public List<PipelineBO> listPipelines(String serviceId) {
    Assert.notEmpty(serviceId, "service can not be empty");

    List<Pipeline> pipelines = list(Wrappers.lambdaQuery(Pipeline.class).eq(Pipeline::getServiceId, serviceId));

    if (CollectionUtils.isEmpty(pipelines)) {
      return new ArrayList<>();
    }

    return pipelines.stream().map(PipelineRepository::convertPipelineBO)
        .collect(Collectors.toList());
  }

  @Override
  public List<PipelineBO> getServicePipelines(String serviceId) {
    List<Pipeline> pipelines = list(Wrappers.<Pipeline>lambdaQuery().eq(Pipeline::getServiceId, serviceId));
    return pipelines.stream().map(PipelineRepository::convertPipelineBO)
            .collect(Collectors.toList());
  }

  @Override
  public PipelineBO getPublishPipeline(String serviceId) {
    Pipeline pipeline = getOne(Wrappers.<Pipeline>lambdaQuery().eq(Pipeline::getServiceId, serviceId)
        .eq(Pipeline::getPipelineType, PipelineType.PUBLISH.getType()));
    return convertPipelineBO(pipeline);
  }

  @Override
  public List<PipelineBO> getSchedulePipelines() {
    List<Pipeline> pipelines = list(Wrappers.<Pipeline>lambdaQuery()
        .eq(Pipeline::getPipelineType, PipelineType.SCHEDULE.getType()));
    return pipelines.stream().map(PipelineRepository::convertPipelineBO)
            .collect(Collectors.toList());
  }

  @Override
  public Integer countAll() {
    return count();
  }

  private static Pipeline convertPipeline(PipelineBO pipelineBO) {
    if (Objects.isNull(pipelineBO)) {
      return null;
    }
    Pipeline pipeline = OrikaUtil.convert(pipelineBO, Pipeline.class);
    Optional.ofNullable(pipelineBO.getPipelineConfig()).ifPresent(config ->
            pipeline.setConfig(JSON.toJSONString(config)));
    return pipeline;
  }

  private static PipelineBO convertPipelineBO(Pipeline pipeline) {
    if (Objects.isNull(pipeline)) {
      return null;
    }
    PipelineBO pipelineBO = OrikaUtil.convert(pipeline, PipelineBO.class);
    pipelineBO.setPipelineConfig(JSON.parseObject(pipeline.getConfig(), PipelineConfig.class));
    return pipelineBO;
  }
}
