package com.zj.domain.repository.pipeline;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.bo.pipeline.NodeBindBO;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
public interface INodeBindRepository {

  /**
   * 保存流水线节点绑定
   * @param nodeBindBO 节点信息
   * @return 是否成功
   */
  boolean saveNodeBind(NodeBindBO nodeBindBO);

  /**
   * 更新流水线节点绑定
   * @param nodeBindBO 节点信息
   * @return 是否成功
   */
  boolean updateNode(NodeBindBO nodeBindBO);

  /**
   * 获取流水线节点
   * @param nodeId 节点ID
   * @return 节点信息
   */
  NodeBindBO getNode(String nodeId);

  /**
   * 删除流水线节点
   * @param nodeId 节点ID
   * @return 是否成功
   */
  Boolean deleteNode(String nodeId);

  /**
   * 分页获取流水线节点
   * @param page 页码
   * @param size 每页数量
   * @param name 节点名称
   * @return 节点列表
   */
  IPage<NodeBindBO> getPageNode(Integer page, Integer size, String name);

  /**
   * 获取所有节点
   * @return 节点列表
   */
  List<NodeBindBO> getAllNodes();
}
