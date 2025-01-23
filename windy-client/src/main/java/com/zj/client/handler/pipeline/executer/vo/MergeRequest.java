package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class MergeRequest extends GitMeta{

  /**
   * 流水线ID
   */
  private String pipelineId;

  /**
   * 项目tag名称
   */
  private String tagName;

  /**
   * tag描述信息
   */
  private String message;

  /**
   * 合并的分支列表
   */
  private List<String> branches;

  /**
   * 是否删除分支
   */
  private String deleteBranch;

  public boolean isDeleteBranch() {
    return Objects.equals(deleteBranch, "1");
  }

}
