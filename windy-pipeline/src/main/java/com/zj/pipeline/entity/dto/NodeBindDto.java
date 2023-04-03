package com.zj.pipeline.entity.dto;

import com.zj.pipeline.entity.po.NodeBind;
import java.util.List;
import lombok.Data;
import org.springframework.beans.BeanUtils;

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
    NodeBind nodeBind = new NodeBind();
    BeanUtils.copyProperties(this, nodeBind);
    return nodeBind;
  }
}
