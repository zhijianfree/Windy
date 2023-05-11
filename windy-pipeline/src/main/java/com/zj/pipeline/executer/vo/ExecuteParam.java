package com.zj.pipeline.executer.vo;

import java.util.List;
import lombok.Data;

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
