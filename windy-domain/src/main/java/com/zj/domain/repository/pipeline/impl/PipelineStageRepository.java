package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.PipelineStageDto;
import com.zj.domain.entity.po.pipeline.PipelineStage;
import com.zj.domain.mapper.pipeline.PipelineStageMapper;
import com.zj.domain.repository.pipeline.IPipelineStageRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
@Repository
public class PipelineStageRepository extends
    ServiceImpl<PipelineStageMapper, PipelineStage> implements IPipelineStageRepository {

  @Override
  public void deletePipelineStages(List<String> stageIds) {
    remove(
        Wrappers.lambdaQuery(PipelineStage.class).in(PipelineStage::getStageId, stageIds));
  }

  @Override
  public PipelineStageDto getPipelineStage(String stageId) {
    PipelineStage pipelineStage = getOne(Wrappers.lambdaQuery(PipelineStage.class)
        .eq(PipelineStage::getStageId, stageId));
    return OrikaUtil.convert(pipelineStage, PipelineStageDto.class);
  }

  @Override
  public boolean updateStage(PipelineStageDto stageDto) {
    PipelineStage pipelineStage = OrikaUtil.convert(stageDto, PipelineStage.class);
    pipelineStage.setUpdateTime(System.currentTimeMillis());
    return update(pipelineStage, Wrappers.lambdaUpdate(PipelineStage.class)
        .eq(PipelineStage::getStageId, pipelineStage.getStageId()));
  }

  @Override
  public boolean deleteStagesByPipelineId(String pipelineId) {
    return remove(
        Wrappers.lambdaQuery(PipelineStage.class).eq(PipelineStage::getPipelineId, pipelineId));
  }

  @Override
  public void saveStage(PipelineStageDto pipelineStageDto) {
    PipelineStage pipelineStage = OrikaUtil.convert(pipelineStageDto, PipelineStage.class);
    Long currentTime = System.currentTimeMillis();
    pipelineStage.setCreateTime(currentTime);
    pipelineStage.setUpdateTime(currentTime);
    save(pipelineStage);
  }

  @Override
  public List<PipelineStageDto> sortPipelineNodes(String pipelineId) {
    List<PipelineStage> pipelineStages = list(
        Wrappers.lambdaQuery(PipelineStage.class).eq(PipelineStage::getPipelineId, pipelineId)
            .orderByAsc(PipelineStage::getType));

    return OrikaUtil.convertList(pipelineStages, PipelineStageDto.class);
  }
}
