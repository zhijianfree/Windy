package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.PipelineNodeDto;
import com.zj.domain.entity.po.pipeline.PipelineNode;
import com.zj.domain.mapper.pipeline.PipelineNodeMapper;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Repository
public class PipelineNodeRepository extends ServiceImpl<PipelineNodeMapper, PipelineNode> implements
    IPipelineNodeRepository {

  @Override
  public PipelineNodeDto getPipelineNode(String pipelineNodeId) {
    PipelineNode pipelineNode = getOne(
        Wrappers.lambdaQuery(PipelineNode.class).eq(PipelineNode::getNodeId, pipelineNodeId));
    return OrikaUtil.convert(pipelineNode, PipelineNodeDto.class);
  }

  @Override
  public List<PipelineNodeDto> getPipelineNodes(String pipelineId) {
    List<PipelineNode> pipelineNodes = list(
        Wrappers.lambdaQuery(PipelineNode.class).eq(PipelineNode::getPipelineId, pipelineId));
    return OrikaUtil.convertList(pipelineNodes, PipelineNodeDto.class);
  }

  @Override
  public List<PipelineNodeDto> getPipelineNodeByIds(List<String> nodeIds) {
    List<PipelineNode> nodes = list(
        Wrappers.lambdaQuery(PipelineNode.class).in(PipelineNode::getNodeId, nodeIds));
    return OrikaUtil.convertList(nodes, PipelineNodeDto.class);
  }

  @Override
  public boolean deleteNodeIds(List<String> nodeIds) {
    return remove(Wrappers.lambdaQuery(PipelineNode.class).in(PipelineNode::getNodeId, nodeIds));
  }

  @Override
  public void updateNode(PipelineNodeDto dto) {
    PipelineNode pipelineNode = OrikaUtil.convert(dto, PipelineNode.class);
    pipelineNode.setUpdateTime(System.currentTimeMillis());
    update(pipelineNode, Wrappers.lambdaUpdate(PipelineNode.class)
        .eq(PipelineNode::getNodeId, pipelineNode.getNodeId()));
  }

  @Override
  public boolean deleteByPipelineId(String pipelineId) {
    return remove(
        Wrappers.lambdaQuery(PipelineNode.class).eq(PipelineNode::getPipelineId, pipelineId));
  }

  @Override
  public void saveNode(PipelineNodeDto nodeDto) {
    PipelineNode pipelineNode = OrikaUtil.convert(nodeDto, PipelineNode.class);
    long dateNow = System.currentTimeMillis();
    pipelineNode.setCreateTime(dateNow);
    pipelineNode.setUpdateTime(dateNow);
    save(pipelineNode);
  }
}
