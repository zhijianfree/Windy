package com.zj.client.config;

import java.io.File;
import java.io.IOException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class GlobalEnvConfig {

  public static final String WINDY = "windy";
  public static final String MAVEN_PATH_KEY = "windy.pipeline.maven.path";
  public static final String WORKSPACE = "windy.pipeline.workspace";
  public static final String DEFAULT_WORKSPACE = "/opt/windy";
  public static final String LOOP_QUERY_TIMEOUT = "windy.loop.query.timeout";
  public static final int MAX_REMOVE_TIME = 2 * 60 * 60 * 1000;
  private final Environment environment;

  public GlobalEnvConfig(Environment environment) {
    this.environment = environment;
  }

  public String getMavenPath() {
    return environment.getProperty(MAVEN_PATH_KEY);
  }

  public String getWorkspace() {
    String path = environment.getProperty(WORKSPACE, DEFAULT_WORKSPACE);
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
    return getWorkspace() + File.separator + service + File.separator + pipelineId;
  }

  private boolean isWorkspaceExist(String workspace) {
    return new File(workspace).exists();
  }

  /**
   * 任务状态查询的超时时间
   * */
  public Integer getLoopQueryTimeout() {
    String timeout = environment.getProperty(LOOP_QUERY_TIMEOUT, String.valueOf(MAX_REMOVE_TIME));
    return Integer.parseInt(timeout);
  }
}
