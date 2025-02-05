package com.zj.client.handler.pipeline.deploy.jar;

import com.zj.client.handler.pipeline.deploy.DeployContext;
import lombok.Builder;
import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/14
 */
@Data
@Builder
public class JarDeployContext extends DeployContext {

  private String localPath;

  private String sshIp;

  private Integer sshPort;

  private String sshUser;

  private String sshPassword;

  private String remotePath;

  private Integer servicePort;

}
