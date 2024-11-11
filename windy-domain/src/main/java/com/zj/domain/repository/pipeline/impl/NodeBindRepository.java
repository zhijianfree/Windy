package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.NodeBindBO;
import com.zj.domain.entity.po.pipeline.NodeBind;
import com.zj.domain.mapper.pipeline.NodeBindMapper;
import com.zj.domain.repository.pipeline.INodeBindRepository;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * @author guyuelan
 * @since 2023/5/19
 */
@Repository
public class NodeBindRepository extends ServiceImpl<NodeBindMapper, NodeBind> implements
    INodeBindRepository {

  @Override
  public boolean saveNodeBind(NodeBindBO nodeBindBO) {
    NodeBind nodeBind = OrikaUtil.convert(nodeBindBO, NodeBind.class);
    nodeBind.setCreateTime(System.currentTimeMillis());
    nodeBind.setUpdateTime(System.currentTimeMillis());
    return save(nodeBind);
  }

  @Override
  public NodeBindBO getNode(String nodeId) {
    NodeBind nodeBind = getOne(Wrappers.lambdaUpdate(NodeBind.class).eq(NodeBind::getNodeId, nodeId));
    return OrikaUtil.convert(nodeBind, NodeBindBO.class);
  }

  @Override
  public boolean updateNode(NodeBindBO nodeBindBO) {
    NodeBind nodeBind = OrikaUtil.convert(nodeBindBO, NodeBind.class);
    nodeBind.setUpdateTime(System.currentTimeMillis());
    return update(nodeBind,
        Wrappers.lambdaUpdate(NodeBind.class).eq(NodeBind::getNodeId, nodeBind.getNodeId()));
  }

  @Override
  public Boolean deleteNode(String nodeId) {
    return remove(Wrappers.lambdaUpdate(NodeBind.class).eq(NodeBind::getNodeId, nodeId));
  }

  @Override
  public IPage<NodeBindBO> getPageNode(Integer page, Integer size, String name) {
    LambdaQueryWrapper<NodeBind> queryWrapper = Wrappers.lambdaQuery(NodeBind.class)
        .orderByDesc(NodeBind::getCreateTime);
    if (!StringUtils.isEmpty(name)) {
      queryWrapper.like(NodeBind::getNodeName, name);
    }
    IPage<NodeBind> actionPage = new Page<>(page, size);
    IPage<NodeBind> nodePage = page(actionPage, queryWrapper);

    IPage<NodeBindBO> result = new Page<>();
    result.setTotal(nodePage.getTotal());
    result.setRecords(OrikaUtil.convertList(nodePage.getRecords(), NodeBindBO.class));
    return result;
  }

  @Override
  public List<NodeBindBO> getAllNodes() {
    List<NodeBind> nodeBinds = list();
    return OrikaUtil.convertList(nodeBinds, NodeBindBO.class);
  }
}
