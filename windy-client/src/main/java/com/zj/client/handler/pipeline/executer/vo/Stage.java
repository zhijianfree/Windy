package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2022/5/23
 */
@Data
public class Stage {

  private String stageName;

  private String stageId;

  private List<TaskNode> nodeList;
}
