package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.PageSize;
import com.zj.common.generate.UniqueIdService;
import com.zj.pipeline.entity.dto.NodeBindDto;
import com.zj.pipeline.entity.dto.PipelineActionDto;
import com.zj.pipeline.entity.po.NodeBind;
import com.zj.pipeline.entity.po.PipelineAction;
import com.zj.pipeline.mapper.NodeBindMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author falcon
 * @since 2023/3/28
 */
@Slf4j
@Service
public class NodeBindService extends ServiceImpl<NodeBindMapper, NodeBind> {

  @Autowired
  private PipelineActionService actionService;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Transactional
  public Boolean createNodes(NodeBindDto nodeBindDto) {
    NodeBind nodeBind = nodeBindDto.toNodeBind();
    nodeBind.setNodeId(uniqueIdService.getUniqueId());
    nodeBind.setUserId("admin");
    nodeBind.setCreateTime(System.currentTimeMillis());
    nodeBind.setUpdateTime(System.currentTimeMillis());
    boolean result = save(nodeBind);

    boolean updateAction = batchUpdateNodeId(nodeBindDto.getExecutors(), nodeBind.getNodeId());
    log.info("update action node id result={}", updateAction);
    return result;
  }


  @Transactional
  public Boolean updateNode(NodeBindDto nodeBindDto) {
    NodeBind nodeBind = nodeBindDto.toNodeBind();
    nodeBind.setUpdateTime(System.currentTimeMillis());
    boolean result = update(nodeBind,
        Wrappers.lambdaUpdate(NodeBind.class).eq(NodeBind::getNodeId, nodeBind.getNodeId()));

    List<PipelineAction> actions = actionService.list(Wrappers.lambdaQuery(PipelineAction.class)
        .eq(PipelineAction::getNodeId, nodeBind.getNodeId()));
    if (!CollectionUtils.isEmpty(actions)) {
      List<String> oldList = actions.stream().map(PipelineAction::getActionId)
          .collect(Collectors.toList());

      List<String> removeList = oldList.stream()
          .filter(actionId -> !nodeBindDto.getExecutors().contains(actionId))
          .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(removeList)){
        actionService.remove(
            Wrappers.lambdaQuery(PipelineAction.class).in(PipelineAction::getActionId, removeList));
      }

      List<String> newList = nodeBindDto.getExecutors().stream()
          .filter(actionId -> !oldList.contains(actionId)).collect(Collectors.toList());
      batchUpdateNodeId(newList, nodeBind.getNodeId());
    }

    return result;
  }

  private boolean batchUpdateNodeId(List<String> actionIds, String nodeId) {
    if (CollectionUtils.isEmpty(actionIds)) {
      return false;
    }

    PipelineAction pipelineAction = new PipelineAction();
    pipelineAction.setNodeId(nodeId);
    return actionService.update(pipelineAction,
        Wrappers.lambdaUpdate(PipelineAction.class).in(PipelineAction::getActionId, actionIds));
  }

  public NodeBind getNode(String nodeId) {
    return getOne(Wrappers.lambdaUpdate(NodeBind.class).eq(NodeBind::getNodeId, nodeId));
  }

  public Boolean deleteNode(String nodeId) {
    return remove(Wrappers.lambdaUpdate(NodeBind.class).eq(NodeBind::getNodeId, nodeId));
  }

  public PageSize<NodeBind> getNodes(Integer page, Integer size, String name) {
    LambdaQueryWrapper<NodeBind> queryWrapper = Wrappers.lambdaQuery(NodeBind.class)
        .orderByDesc(NodeBind::getCreateTime);
    if (!StringUtils.isEmpty(name)) {
      queryWrapper.like(NodeBind::getNodeName, name);
    }
    IPage<NodeBind> actionIPage = new Page<>(page, size);
    IPage<NodeBind> list = page(actionIPage, queryWrapper);
    PageSize<NodeBind> pageSize = new PageSize<>();
    pageSize.setTotal(list.getTotal());
    pageSize.setData(list.getRecords());
    return pageSize;
  }

  public List<PipelineActionDto> getNodeExecutors(String nodeId) {
    List<PipelineAction> actions = actionService.list(
        Wrappers.lambdaQuery(PipelineAction.class).eq(PipelineAction::getNodeId, nodeId));

    return actions.stream().map(PipelineActionDto::toPipelineActionDto)
        .collect(Collectors.toList());
  }

  public List<NodeBind> getAllNodes() {
      return list();
  }
}
