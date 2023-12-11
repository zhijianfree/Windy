package com.zj.client.handler.pipeline.executer.vo;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Data
public class DeployRequest {

  private String pipelineId;

  private String gitUrl;

  private Object params;

  private String serverPort;

  private Integer deployType;

  @Data
  public static class SSHParams{
    private String remotePath;

    private String sshIp;

    private Integer sshPort;

    private String user;

    private String password;
  }
}
