package com.zj.client.entity.dto;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Data
public class BuildParam {

  private String recordId;

  private String pipelineId;

  private String gitUrl;

  private String pomPath;

  private String branch;
}
