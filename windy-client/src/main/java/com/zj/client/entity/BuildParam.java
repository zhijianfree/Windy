package com.zj.client.entity;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/3/29
 */
@Data
public class BuildParam {

  private String recordId;

  private String gitUrl;

  private String pomPath;

  private String branch;
}
