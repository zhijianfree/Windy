package com.zj.client.pipeline.executer.vo;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/6/15
 */
@Data
public class DeployRequest {
  private String remotePath;

  private String sshIp;

  private Integer sshPort;

  private String deployType;
}
