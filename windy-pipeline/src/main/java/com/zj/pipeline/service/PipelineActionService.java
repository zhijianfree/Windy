package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.PageSize;
import com.zj.common.generate.UniqueIdService;
import com.zj.pipeline.entity.dto.PipelineActionDto;
import com.zj.pipeline.entity.po.PipelineAction;
import com.zj.pipeline.mapper.PipelineActionMapper;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author falcon
 * @since 2023/3/27
 */
@Service
public class PipelineActionService extends ServiceImpl<PipelineActionMapper, PipelineAction> {

  @Autowired
  private UniqueIdService uniqueIdService;

  public Boolean createAction(PipelineActionDto actionDto) {
    PipelineAction pipelineAction = actionDto.toPipelineAction();
    pipelineAction.setActionId(uniqueIdService.getUniqueId());
    pipelineAction.setCreateTime(System.currentTimeMillis());
    pipelineAction.setUpdateTime(System.currentTimeMillis());
    return save(pipelineAction);
  }

  public PipelineActionDto getAction(String actionId) {
    PipelineAction action = getOne(
        Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getActionId, actionId));
    if (Objects.isNull(action)) {
      return null;
    }
    return PipelineActionDto.toPipelineActionDto(action);
  }

  public Boolean updateAction(PipelineActionDto actionDto) {
    PipelineAction pipelineAction = actionDto.toPipelineAction();
    pipelineAction.setUpdateTime(System.currentTimeMillis());
    return update(pipelineAction, Wrappers.lambdaUpdate(PipelineAction.class)
        .eq(PipelineAction::getActionId, actionDto.getActionId()));
  }

  public Boolean deleteAction(String actionId) {
    return remove(
        Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getActionId, actionId));
  }

  public PageSize<PipelineActionDto> getActions(Integer page, Integer size, String name) {
    LambdaQueryWrapper<PipelineAction> queryWrapper = Wrappers.lambdaQuery(PipelineAction.class)
        .orderByDesc(PipelineAction::getCreateTime);
    if (!StringUtils.isEmpty(name)){
      queryWrapper.like(PipelineAction::getActionName, name);
    }
    IPage<PipelineAction> actionIPage = new Page<>(page, size);
    IPage<PipelineAction> list = page(actionIPage, queryWrapper);

    List<PipelineActionDto> actionList = list.getRecords().stream()
        .map(PipelineActionDto::toPipelineActionDto)
        .collect(Collectors.toList());

    PageSize<PipelineActionDto> pageSize = new PageSize<>();
    pageSize.setTotal(list.getTotal());
    pageSize.setData(actionList);
    return pageSize;
  }
}
