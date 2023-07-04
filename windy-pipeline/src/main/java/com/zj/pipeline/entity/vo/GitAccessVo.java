package com.zj.pipeline.entity.vo;

import lombok.Data;

/**
 * git访问信息
 * */
@Data
public class GitAccessVo {

  private String gitDomain;

  private String accessToken;

  private String owner;

  private String gitType;

}
