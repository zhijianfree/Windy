package com.zj.domain.repository.pipeline.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.common.entity.pipeline.ConfigDetail;
import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.bo.pipeline.PipelineNodeBO;
import com.zj.domain.entity.po.pipeline.PipelineNode;
import com.zj.domain.mapper.pipeline.PipelineNodeMapper;
import com.zj.domain.repository.pipeline.IPipelineNodeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/5/15
 */
@Repository
public class PipelineNodeRepository extends ServiceImpl<PipelineNodeMapper, PipelineNode> implements
        IPipelineNodeRepository {

    @Override
    public PipelineNodeBO getPipelineNode(String pipelineNodeId) {
        PipelineNode pipelineNode = getOne(
                Wrappers.lambdaQuery(PipelineNode.class).eq(PipelineNode::getNodeId, pipelineNodeId));
        return convertPipelineNode(pipelineNode);
    }

    @Override
    public List<PipelineNodeBO> getPipelineNodes(String pipelineId) {
        List<PipelineNode> pipelineNodes = list(
                Wrappers.lambdaQuery(PipelineNode.class).eq(PipelineNode::getPipelineId, pipelineId));
        return pipelineNodes.stream().map(PipelineNodeRepository::convertPipelineNode).collect(Collectors.toList());
    }

    @Override
    public List<PipelineNodeBO> getPipelineNodeByIds(List<String> nodeIds) {
        List<PipelineNode> pipelineNodes = list(
                Wrappers.lambdaQuery(PipelineNode.class).in(PipelineNode::getNodeId, nodeIds));
        return pipelineNodes.stream().map(PipelineNodeRepository::convertPipelineNode).collect(Collectors.toList());
    }

    @Override
    public boolean deleteNodeIds(List<String> nodeIds) {
        return remove(Wrappers.lambdaQuery(PipelineNode.class).in(PipelineNode::getNodeId, nodeIds));
    }

    @Override
    public void updateNode(PipelineNodeBO dto) {
        PipelineNode pipelineNode = convertPipelineNode(dto);
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
    public void saveNode(PipelineNodeBO nodeDto) {
        PipelineNode pipelineNode = convertPipelineNode(nodeDto);
        long dateNow = System.currentTimeMillis();
        pipelineNode.setCreateTime(dateNow);
        pipelineNode.setUpdateTime(dateNow);
        save(pipelineNode);
    }

    private static PipelineNodeBO convertPipelineNode(PipelineNode pipelineNode) {
        PipelineNodeBO pipelineNodeBO = OrikaUtil.convert(pipelineNode, PipelineNodeBO.class);
        pipelineNodeBO.setConfigDetail(JSON.parseObject(pipelineNode.getConfig(), ConfigDetail.class));
        return pipelineNodeBO;
    }

    private static PipelineNode convertPipelineNode(PipelineNodeBO pipelineNodeBO) {
        PipelineNode pipelineNode = OrikaUtil.convert(pipelineNodeBO, PipelineNode.class);
        Optional.ofNullable(pipelineNodeBO.getConfigDetail()).ifPresent(configDetail ->
                pipelineNode.setConfig(JSON.toJSONString(pipelineNodeBO.getConfigDetail())));
        return pipelineNode;
    }
}
