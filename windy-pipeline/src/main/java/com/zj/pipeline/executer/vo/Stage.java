package com.zj.pipeline.executer.vo;

import java.util.List;
import lombok.Data;

/**
 * @author falcon
 * @since 2022/5/23
 */
@Data
public class Stage {

  private String stageName;

  private String stageId;

  private List<TaskNode> nodeList;
}
