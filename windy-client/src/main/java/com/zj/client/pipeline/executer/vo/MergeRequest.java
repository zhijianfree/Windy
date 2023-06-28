package com.zj.client.pipeline.executer.vo;

import java.util.List;
import lombok.Data;

@Data
public class MergeRequest {

  private String pipelineId;

  private List<String> branches;

  private String gitUrl;

}
