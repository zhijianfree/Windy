package com.zj.client.config;

import java.io.File;
import java.io.IOException;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class GlobalEnvConfig {

  public static final String WINDY = "windy";
  public static final String SERVICES = "services";

  @Value("${windy.pipeline.workspace:/opt/windy}")
  private String workspace;
  
  @Getter
  @Value("${windy.loop.query.timeout:7200000}")
  private Integer loopQueryTimeout;

  @Getter
  @Value("${windy.pipeline.maven.path")
  private String mavenPath;

  public String getWorkspace() {
    if (!isWorkspaceExist(workspace)) {
      try {
        return new File("").getCanonicalPath() + File.separator + WINDY;
      } catch (IOException ignore) {
      }
    }
    return workspace;
  }

  /**
   * 获取流水线构建过程中的工作路径
   * */
  public String getPipelineWorkspace(String service, String pipelineId) {
    return getWorkspace() + File.separator + SERVICES + File.separator + service + File.separator + pipelineId;
  }

  private boolean isWorkspaceExist(String workspace) {
    return new File(workspace).exists();
  }

}
