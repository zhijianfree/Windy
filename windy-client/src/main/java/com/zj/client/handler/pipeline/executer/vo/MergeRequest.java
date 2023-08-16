package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class MergeRequest extends GitMeta{

  private String pipelineId;

  private List<String> branches;

  private String deleteBranch;

  public boolean isDeleteBranch() {
    return Objects.equals(deleteBranch, "1");
  }

}
