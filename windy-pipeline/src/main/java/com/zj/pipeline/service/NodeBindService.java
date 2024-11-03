package com.zj.pipeline.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.common.adapter.auth.IAuthService;
import com.zj.common.adapter.uuid.UniqueIdService;
import com.zj.common.entity.dto.PageSize;
import com.zj.domain.entity.bo.pipeline.NodeBindBO;
import com.zj.domain.entity.bo.pipeline.PipelineActionBO;
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
  private final IAuthService authService;

  public NodeBindService(PipelineActionService actionService,
                         INodeBindRepository nodeBindRepository, UniqueIdService uniqueIdService, IAuthService authService) {
    this.actionService = actionService;
    this.nodeBindRepository = nodeBindRepository;
    this.uniqueIdService = uniqueIdService;
    this.authService = authService;
  }

  @Transactional
  public Boolean createNodes(NodeBindBO nodeBindBO) {
    nodeBindBO.setNodeId(uniqueIdService.getUniqueId());
    nodeBindBO.setUserId(authService.getCurrentUserId());
    boolean result = nodeBindRepository.saveNodeBind(nodeBindBO);

    boolean updateAction = batchUpdateNodeId(nodeBindBO.getNodeId(), nodeBindBO.getExecutors());
    log.info("update action node id result={}", updateAction);
    return result;
  }


  @Transactional
  public Boolean updateNode(NodeBindBO nodeBindBO) {
    boolean result = nodeBindRepository.updateNode(nodeBindBO);
    List<PipelineActionBO> actions = actionService.getActionsByNodeId(nodeBindBO.getNodeId());
    if (CollectionUtils.isEmpty(actions)) {
      return batchUpdateNodeId(nodeBindBO.getNodeId(), nodeBindBO.getExecutors());
    }

    List<String> oldList = actions.stream().map(PipelineActionBO::getActionId)
        .collect(Collectors.toList());

    List<String> removeList = oldList.stream()
        .filter(actionId -> !nodeBindBO.getExecutors().contains(actionId))
        .collect(Collectors.toList());
    if (!CollectionUtils.isEmpty(removeList)) {
      boolean removeResult = actionService.batchDelete(removeList);
      log.info("delete action list result={}", removeResult);
    }

    List<String> newList = nodeBindBO.getExecutors().stream()
        .filter(actionId -> !oldList.contains(actionId)).collect(Collectors.toList());
    batchUpdateNodeId(nodeBindBO.getNodeId(), newList);
    return result;
  }

  private boolean batchUpdateNodeId(String nodeId, List<String> actionIds) {
    if (CollectionUtils.isEmpty(actionIds)) {
      return false;
    }
    return actionService.actionsBindNode(nodeId, actionIds);
  }

  public NodeBindBO getNode(String nodeId) {
    return nodeBindRepository.getNode(nodeId);
  }

  public Boolean deleteNode(String nodeId) {
    return nodeBindRepository.deleteNode(nodeId);
  }

  public PageSize<NodeBindBO> getNodes(Integer page, Integer size, String name) {
    IPage<NodeBindBO> pageList = nodeBindRepository.getPageNode(page, size, name);
    PageSize<NodeBindBO> pageSize = new PageSize<>();
    pageSize.setTotal(pageList.getTotal());
    pageSize.setData(pageList.getRecords());
    return pageSize;
  }

  public List<PipelineActionBO> getNodeExecutors(String nodeId) {
    return actionService.getActionsByNodeId(nodeId);
  }

  public List<NodeBindBO> getAllNodes() {
    return nodeBindRepository.getAllNodes();
  }
}
