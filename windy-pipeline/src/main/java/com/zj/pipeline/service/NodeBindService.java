package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.model.PageSize;
import com.zj.common.generate.UniqueIdService;
import com.zj.domain.entity.dto.pipeline.NodeBindDto;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.domain.entity.po.pipeline.NodeBind;
import com.zj.domain.entity.po.pipeline.PipelineAction;
import com.zj.domain.mapper.pipeline.NodeBindMapper;
import com.zj.domain.repository.pipeline.INodeBindRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author guyuelan
 * @since 2023/3/28
 */
@Slf4j
@Service
public class NodeBindService {

  @Autowired
  private PipelineActionService actionService;

  @Autowired
  private INodeBindRepository nodeBindRepository;

  @Autowired
  private UniqueIdService uniqueIdService;

  @Transactional
  public Boolean createNodes(NodeBindDto nodeBindDto) {
    nodeBindDto.setNodeId(uniqueIdService.getUniqueId());
    boolean result = nodeBindRepository.saveNodeBind(nodeBindDto);

    boolean updateAction = batchUpdateNodeId(nodeBindDto.getExecutors(), nodeBindDto.getNodeId());
    log.info("update action node id result={}", updateAction);
    return result;
  }


  @Transactional
  public Boolean updateNode(NodeBindDto nodeBindDto) {
    boolean result = nodeBindRepository.updateNode(nodeBindDto);
    List<PipelineAction> actions = actionService.getActionsByNodeId(nodeBindDto.getNodeId());
    if (!CollectionUtils.isEmpty(actions)) {
      List<String> oldList = actions.stream().map(PipelineAction::getActionId)
          .collect(Collectors.toList());

      List<String> removeList = oldList.stream()
          .filter(actionId -> !nodeBindDto.getExecutors().contains(actionId))
          .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(removeList)) {
        actionService.batchDelete(removeList);
      }

      List<String> newList = nodeBindDto.getExecutors().stream()
          .filter(actionId -> !oldList.contains(actionId)).collect(Collectors.toList());
      batchUpdateNodeId(newList, nodeBindDto.getNodeId());
    }

    return result;
  }

  private boolean batchUpdateNodeId(List<String> actionIds, String nodeId) {
    if (CollectionUtils.isEmpty(actionIds)) {
      return false;
    }

    PipelineActionDto pipelineAction = new PipelineActionDto();
    pipelineAction.setNodeId(nodeId);
    return actionService.updateAction(pipelineAction);
  }

  public NodeBindDto getNode(String nodeId) {
    return nodeBindRepository.getNode(nodeId);
  }

  public Boolean deleteNode(String nodeId) {
    return nodeBindRepository.deleteNode(nodeId);
  }

  public PageSize<NodeBindDto> getNodes(Integer page, Integer size, String name) {
    IPage<NodeBindDto> pageList = nodeBindRepository.getPageNode(page, size, name);
    PageSize<NodeBindDto> pageSize = new PageSize<>();
    pageSize.setTotal(pageList.getTotal());
    pageSize.setData(pageList.getRecords());
    return pageSize;
  }

  public List<PipelineActionDto> getNodeExecutors(String nodeId) {
    List<PipelineAction> actions = actionService.getActionsByNodeId(nodeId);
    return actions.stream().map(PipelineActionService::toPipelineActionDto)
        .collect(Collectors.toList());
  }

  public List<NodeBindDto> getAllNodes() {
    return nodeBindRepository.getAllNodes();
  }
}
