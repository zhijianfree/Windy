package com.zj.client.handler.pipeline.executer.vo;

import java.util.List;
import java.util.Objects;
import lombok.Data;

@Data
public class MergeRequest {

  private String pipelineId;

  private List<String> branches;

  private String deleteBranch;

  private String gitUrl;

  public boolean isDeleteBranch() {
    return Objects.equals(deleteBranch, "1");
  }

}
