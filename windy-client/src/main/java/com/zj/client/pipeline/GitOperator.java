package com.zj.client.pipeline;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/3/29
 */
@Component
public class GitOperator {

  @Value("${windy.pipeline.git.user:windy}")
  private String user;

  @Value("${windy.pipeline.git.password:windy!123}")
  private String password;

  @Value("${windy.pipeline.git.workspace:/opt/windy/}")
  private String workspace;

  public void pullCode(String gitUrl, String branch) throws Exception {
    String serviceName = getServiceFromUrl(gitUrl);
    // 判断本地目录是否存在
    String serviceDir = workspace + File.separator + serviceName;
    createIfNotExist(serviceDir);

    // 提供用户名和密码的验证
    UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(user,
        password);

    // clone 仓库到指定目录
    Git git = Git.cloneRepository().setURI(gitUrl).setDirectory(new File(serviceDir))
        .setBranch(branch).setCredentialsProvider(provider).call();
  }

  private void createIfNotExist(String serviceDir) {
    File dir = new File(serviceDir);
    try {
      if (!dir.exists()) {
        FileUtils.createParentDirectories(dir);
        return;
      }
      FileUtils.cleanDirectory(dir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getServiceFromUrl(String gitUrl) {
    if (StringUtils.isBlank(gitUrl)) {
      return null;
    }
    String[] strings = gitUrl.split("/");
    return strings[strings.length - 1].split("\\.")[0];
  }
}
