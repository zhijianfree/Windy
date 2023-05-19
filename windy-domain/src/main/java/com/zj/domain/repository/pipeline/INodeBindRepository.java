package com.zj.domain.repository.pipeline;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zj.domain.entity.dto.pipeline.NodeBindDto;
import com.zj.domain.entity.po.pipeline.NodeBind;
import java.util.List;

/**
 * @author falcon
 * @since 2023/5/19
 */
public interface INodeBindRepository {

  boolean saveNodeBind(NodeBindDto nodeBindDto);

  boolean updateNode(NodeBindDto nodeBindDto);

  NodeBindDto getNode(String nodeId);

  Boolean deleteNode(String nodeId);

  IPage<NodeBindDto> getPageNode(Integer page, Integer size, String name);

  List<NodeBindDto> getAllNodes();
}
