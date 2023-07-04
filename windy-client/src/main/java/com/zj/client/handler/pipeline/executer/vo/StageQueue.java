package com.zj.client.handler.pipeline.executer.vo;

import java.util.List;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/5/9
 */
@Data
public class StageQueue {

  private String stageId;

  private List<TaskNode> taskNodes;

}
