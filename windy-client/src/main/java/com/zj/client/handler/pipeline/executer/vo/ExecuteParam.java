package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2022/5/23
 */
@Data
public class ExecuteParam {

  private String pipelineId;

  private String name;

  private List<Stage> stages;

  private PipeLineConfig pipeLineConfig;
}
