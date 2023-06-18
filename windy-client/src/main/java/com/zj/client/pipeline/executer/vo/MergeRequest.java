package com.zj.client.pipeline.executer.vo;

import lombok.Data;

@Data
public class MergeRequest {

  private String sourceBranch;

  private String gitUrl;

}
