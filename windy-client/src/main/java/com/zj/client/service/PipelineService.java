package com.zj.client.service;

import com.zj.client.entity.BuildParam;
import com.zj.client.entity.ResponseModel;
import com.zj.client.pipeline.GitOperator;
import com.zj.client.pipeline.MavenOperator;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author falcon
 * @since 2023/3/29
 */
@Slf4j
@Service
public class PipelineService {

  @Value("windy.pipeline.git.workspace:/opt/windy/")
  private String workspace;
  @Autowired
  private GitOperator gitOperator;

  @Autowired
  private MavenOperator mavenOperator;

  public ResponseModel buildCode(BuildParam buildParam) {
    try {
      gitOperator.pullCode(buildParam.getGitUrl(), buildParam.getBranch());

      String serviceName = GitOperator.getServiceFromUrl(buildParam.getGitUrl());

      String pomPath =
          workspace + File.separator + serviceName + File.separator + buildParam.getPomPath();
      Integer exitCode = mavenOperator.build(pomPath);
      log.info("get maven exit code={}", exitCode);
      return new ResponseModel(true, "构建成功");
    } catch (Exception e) {
      log.error("buildCode error", e);
      return new ResponseModel(false, e.getMessage());
    }

  }
}
