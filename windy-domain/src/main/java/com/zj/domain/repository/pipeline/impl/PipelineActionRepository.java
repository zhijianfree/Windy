package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.domain.entity.po.pipeline.PipelineAction;
import com.zj.domain.mapper.pipeline.PipelineActionMapper;
import com.zj.domain.repository.pipeline.IPipelineActionRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * @author falcon
 * @since 2023/5/15
 */
@Repository
public class PipelineActionRepository extends
    ServiceImpl<PipelineActionMapper, PipelineAction> implements IPipelineActionRepository {

  @Override
  public Boolean createAction(PipelineActionDto actionDto) {
    PipelineAction pipelineAction = OrikaUtil.convert(actionDto, PipelineAction.class);
    long dateNow = System.currentTimeMillis();
    pipelineAction.setCreateTime(dateNow);
    pipelineAction.setUpdateTime(dateNow);
    return save(pipelineAction);
  }

  @Override
  public PipelineActionDto getAction(String actionId) {
    PipelineAction action = getOne(
        Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getActionId, actionId));
    if (Objects.isNull(action)) {
      return null;
    }
    return OrikaUtil.convert(action, PipelineActionDto.class);
  }

  @Override
  public Boolean updateAction(PipelineActionDto actionDto) {
    PipelineAction pipelineAction = OrikaUtil.convert(actionDto, PipelineAction.class);
    return update(pipelineAction, Wrappers.lambdaUpdate(PipelineAction.class)
        .eq(PipelineAction::getActionId, actionDto.getActionId()));
  }

  @Override
  public Boolean deleteAction(String actionId) {
    return remove(
        Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getActionId, actionId));
  }

  @Override
  public IPage<PipelineAction> getActions(Integer page, Integer size, String name) {
    LambdaQueryWrapper<PipelineAction> queryWrapper = Wrappers.lambdaQuery(PipelineAction.class)
        .orderByDesc(PipelineAction::getCreateTime);
    if (!StringUtils.isEmpty(name)) {
      queryWrapper.like(PipelineAction::getActionName, name);
    }
    IPage<PipelineAction> actionIPage = new Page<>(page, size);
    return page(actionIPage, queryWrapper);
  }

  @Override
  public List<PipelineAction> getActionsByNodeId(String nodeId) {
    return list(Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getNodeId, nodeId));
  }

  @Override
  public boolean batchDelete(List<String> removeList) {
    return remove(
        Wrappers.lambdaQuery(PipelineAction.class).in(PipelineAction::getActionId, removeList));
  }
}
