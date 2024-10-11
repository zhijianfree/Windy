package com.zj.domain.repository.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.model.PageSize;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.ActionParam;
import com.zj.domain.entity.dto.pipeline.CompareResult;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.domain.entity.po.pipeline.PipelineAction;
import com.zj.domain.mapper.pipeline.PipelineActionMapper;
import com.zj.domain.repository.pipeline.IPipelineActionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
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
        pipelineAction.setHeaders(JSON.toJSONString(actionDto.getHeaders()));
        pipelineAction.setResult(JSON.toJSONString(actionDto.getCompareResults()));
        pipelineAction.setParamDetail(JSON.toJSONString(actionDto.getParamList()));
        return save(pipelineAction);
    }

    @Override
    public PipelineActionDto getAction(String actionId) {
        PipelineAction action = getOne(
                Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getActionId, actionId));
        return Optional.ofNullable(action).map(this::convertActionDto).orElse(null);
    }

    private PipelineActionDto convertActionDto(PipelineAction action) {
        PipelineActionDto actionDto = OrikaUtil.convert(action, PipelineActionDto.class);
        actionDto.setHeaders(JSONObject.parseObject(action.getHeaders(), new TypeReference<Map<String, String>>() {
        }));
        actionDto.setCompareResults(JSON.parseArray(action.getResult(), CompareResult.class));
        actionDto.setParamList(JSON.parseArray(action.getParamDetail(), ActionParam.class));
        actionDto.setLoopExpression(JSON.parseObject(action.getQueryExpression(), CompareResult.class));
        return actionDto;
    }

    @Override
    public Boolean updateAction(PipelineActionDto actionDto) {
        PipelineAction pipelineAction = OrikaUtil.convert(actionDto, PipelineAction.class);
        Optional.ofNullable(actionDto.getHeaders()).ifPresent(headers -> pipelineAction.setHeaders(JSON.toJSONString(headers)));
        Optional.ofNullable(actionDto.getParamList()).ifPresent(params -> pipelineAction.setParamDetail(JSON.toJSONString(params)));
        Optional.ofNullable(actionDto.getCompareResults()).ifPresent(compareResults -> pipelineAction.setResult(JSON.toJSONString(compareResults)));
        return update(pipelineAction, Wrappers.lambdaUpdate(PipelineAction.class)
                .eq(PipelineAction::getActionId, actionDto.getActionId()));
    }

    @Override
    public Boolean actionsBindNode(String nodeId, List<String> actionIds) {
        PipelineAction pipelineAction = new PipelineAction();
        pipelineAction.setNodeId(nodeId);
        return update(pipelineAction,
                Wrappers.lambdaUpdate(PipelineAction.class).in(PipelineAction::getActionId, actionIds));
    }

    @Override
    public Boolean deleteAction(String actionId) {
        return remove(
                Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getActionId, actionId));
    }

    @Override
    public PageSize<PipelineActionDto> getActions(Integer page, Integer size, String name) {
        LambdaQueryWrapper<PipelineAction> queryWrapper = Wrappers.lambdaQuery(PipelineAction.class)
                .orderByDesc(PipelineAction::getCreateTime);
        if (!StringUtils.isEmpty(name)) {
            queryWrapper.like(PipelineAction::getActionName, name);
        }
        IPage<PipelineAction> actionIPage = new Page<>(page, size);
        IPage<PipelineAction> queryPage = page(actionIPage, queryWrapper);
        PageSize<PipelineActionDto> pageSize = new PageSize<>();
        pageSize.setTotal(queryPage.getTotal());
        pageSize.setData(queryPage.getRecords().stream().map(this::convertActionDto).collect(Collectors.toList()));
        return pageSize;
    }

    @Override
    public List<PipelineActionDto> getActionsByNodeId(String nodeId) {
        List<PipelineAction> actions = list(Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getNodeId, nodeId));
        return Optional.ofNullable(actions).map(actionList -> actionList.stream().map(this::convertActionDto)
                .collect(Collectors.toList())).orElseGet(ArrayList::new);
    }

    @Override
    public boolean batchDelete(List<String> removeList) {
        return remove(
                Wrappers.lambdaQuery(PipelineAction.class).in(PipelineAction::getActionId, removeList));
    }
}
