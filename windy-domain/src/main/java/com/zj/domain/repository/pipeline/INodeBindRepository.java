package com.zj.domain.repository.pipeline;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.pipeline.NodeBindBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface INodeBindRepository {

  boolean saveNodeBind(NodeBindBO nodeBindBO);

  boolean updateNode(NodeBindBO nodeBindBO);

  NodeBindBO getNode(String nodeId);

  Boolean deleteNode(String nodeId);

  IPage<NodeBindBO> getPageNode(Integer page, Integer size, String name);

  List<NodeBindBO> getAllNodes();
}
