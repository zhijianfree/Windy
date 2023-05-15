package com.zj.domain.entity.dto.pipeline;

import com.zj.common.utils.OrikaUtil;
import com.zj.domain.entity.po.pipeline.NodeBind;
import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/28
 */
@Data
public class NodeBindDto {

  private String nodeId;

  private String nodeName;

  private String description;

  private List<String> executors;

  public NodeBind toNodeBind(){
    return OrikaUtil.convert(this, NodeBind.class);
  }
}
