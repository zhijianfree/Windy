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

  private String imageName;

  private Integer replicas;

  @Data
  public static class SSHParams{
    private String remotePath;

    private String sshIp;

    private Integer sshPort;

    private String user;

    private String password;
  }

  @Data
  public static class K8SParams{
    private String apiService;

    private String token;

    private String repository;

    private String namespace;
  }
}
