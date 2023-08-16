package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.generate.UniqueIdService;
import com.zj.common.model.PageSize;
import com.zj.domain.entity.dto.pipeline.NodeBindDto;
import com.zj.domain.entity.dto.pipeline.PipelineActionDto;
import com.zj.domain.entity.po.pipeline.PipelineAction;
import com.zj.domain.repository.pipeline.INodeBindRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * @author guyuelan
 * @since 2023/3/28
 */
@Slf4j
@Service
public class NodeBindService {
  private final PipelineActionService actionService;
  private final INodeBindRepository nodeBindRepository;
  private final UniqueIdService uniqueIdService;

  public NodeBindService(PipelineActionService actionService,
      INodeBindRepository nodeBindRepository, UniqueIdService uniqueIdService) {
    this.actionService = actionService;
    this.nodeBindRepository = nodeBindRepository;
    this.uniqueIdService = uniqueIdService;
  }

  @Transactional
  public Boolean createNodes(NodeBindDto nodeBindDto) {
    nodeBindDto.setNodeId(uniqueIdService.getUniqueId());
    boolean result = nodeBindRepository.saveNodeBind(nodeBindDto);

    boolean updateAction = batchUpdateNodeId(nodeBindDto.getNodeId(), nodeBindDto.getExecutors());
    log.info("update action node id result={}", updateAction);
    return result;
  }


  @Transactional
  public Boolean updateNode(NodeBindDto nodeBindDto) {
    boolean result = nodeBindRepository.updateNode(nodeBindDto);
    List<PipelineAction> actions = actionService.getActionsByNodeId(nodeBindDto.getNodeId());
    if (CollectionUtils.isEmpty(actions)) {
      return batchUpdateNodeId(nodeBindDto.getNodeId(), nodeBindDto.getExecutors());
    }

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
    batchUpdateNodeId(nodeBindDto.getNodeId(), newList);
    return result;
  }

  private boolean batchUpdateNodeId(String nodeId, List<String> actionIds) {
    if (CollectionUtils.isEmpty(actionIds)) {
      return false;
    }
    return actionService.actionsBindNode(nodeId, actionIds);
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
