package com.zj.pipeline.entity.dto;

import com.zj.common.utils.OrikaUtil;
import com.zj.pipeline.entity.po.NodeBind;
import java.util.List;
import lombok.Data;

/**
 * @author falcon
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
