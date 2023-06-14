package com.zj.client.deploy.jar;

import com.zj.client.deploy.DeployContext;
import lombok.Data;

/**
 * @author falcon
 * @since 2023/6/14
 */
@Data
public class JarDeployContext extends DeployContext {

  private String localPath;

  private String sshIp;

  private Integer sshPort;

  private String sshUser;

  private String sshPassword;

  private String remotePath;

  private String remoteSHFile;

}
