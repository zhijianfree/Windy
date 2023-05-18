package com.zj.domain.repository.pipeline.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.dto.pipeline.PipelineNodeDTO;
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
  public PipelineNodeDTO getPipelineNode(String pipelineNodeId) {
    PipelineNode pipelineNode = getOne(
        Wrappers.lambdaQuery(PipelineNode.class).eq(PipelineNode::getNodeId, pipelineNodeId));
    return OrikaUtil.convert(pipelineNode, PipelineNodeDTO.class);
  }

  @Override
  public List<PipelineNodeDTO> getPipelineNodes(String pipelineId) {
    List<PipelineNode> pipelineNodes = list(
        Wrappers.lambdaQuery(PipelineNode.class).eq(PipelineNode::getPipelineId, pipelineId));
    return OrikaUtil.convertList(pipelineNodes, PipelineNodeDTO.class);
  }
}
