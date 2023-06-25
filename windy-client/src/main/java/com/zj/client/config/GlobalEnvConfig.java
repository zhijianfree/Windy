package com.zj.client.config;

import com.zj.client.utils.Utils;
import java.io.File;
import java.io.IOException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class GlobalEnvConfig {

  public static final String WINDY = "windy";
  public static final String GIT_USER = "windy.pipeline.git.user";
  public static final String GIT_PASSWORD = "windy.pipeline.git.password";
  public static final String DEFAULT_GIT_PWD = "zhijian137899";
  public static final String DEFAULT_GIT_USER = "guyuelan";

  public static final String DEPLOY_SSH_USER = "windy.deploy.ssh.user";
  public static final String DEPLOY_SSH_PWD = "windy.deploy.ssh.pwd";
  public static final String DEFAULT_SSH_USER = "windy";
  public static final String DEFAULT_SSH_PWD = "windy!123";

  public static final String MAVEN_PATH_KEY = "windy.pipeline.maven.path";
  public static final String GIT_WORKSPACE = "windy.pipeline.git.workspace";
  public static final String DEFAULT_GIT_WORKSPACE = "/opt/windy";
  private final Environment environment;

  public GlobalEnvConfig(Environment environment) {
    this.environment = environment;
  }

  public String getSShUser() {
    return environment.getProperty(DEPLOY_SSH_USER, DEFAULT_SSH_USER);
  }

  public String getSSHPassword() {
    return environment.getProperty(DEPLOY_SSH_PWD, DEFAULT_SSH_PWD);
  }

  public String getGitUser() {
    return environment.getProperty(GIT_USER, DEFAULT_GIT_USER);
  }

  public String getGitPassword() {
    return environment.getProperty(GIT_PASSWORD, DEFAULT_GIT_PWD);
  }

  public String getMavenPath() {
    return environment.getProperty(MAVEN_PATH_KEY);
  }

  public String getGitWorkspace() {
    String path = environment.getProperty(GIT_WORKSPACE, DEFAULT_GIT_WORKSPACE);
    if (!isWorkspaceExist(path)) {
      try {
        path = new File("").getCanonicalPath() + File.separator + WINDY;
      } catch (IOException ignore) {
      }
    }
    return path;
  }

  /**
   * 获取流水线构建过程中的工作路径
   * */
  public String getPipelineWorkspace(String service, String pipelineId) {
    return getGitWorkspace() + File.separator + service + File.separator + pipelineId;
  }

  private boolean isWorkspaceExist(String workspace) {
    return new File(workspace).exists();
  }
}
