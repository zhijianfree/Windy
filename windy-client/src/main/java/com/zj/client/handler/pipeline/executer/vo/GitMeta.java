package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/7/12
 */
@Data
public class GitMeta {

  /**
   * 服务Git地址
   */
  private String gitUrl;

  /**
   * token拥有者
   */
  private String tokenName;

  /**
   * 访问Git的token
   */
  private String token;

  /**
   * 主干分支名称
   */
  private String mainBranch;
}
