package com.zj.client.pipeline.git;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Component
public class GitOperator {

  public static final String GIT_USER = "windy.pipeline.git.user";
  public static final String GIT_PASSWORD = "windy.pipeline.git.password";
  public static final String DEFAULT_PWD = "windy!123";
  public static final String DEFAULT_USER = "windy";
  private final Environment environment;

  public GitOperator(Environment environment) {
    this.environment = environment;
  }

  public void pullCodeFromGit(String gitUrl, String branch, String workspace) throws Exception {
    String serviceName = getServiceFromUrl(gitUrl);
    // 判断本地目录是否存在
    String serviceDir = workspace + File.separator + serviceName;
    createIfNotExist(serviceDir);

    // clone 仓库到指定目录
    // 提供用户名和密码的验证
    String user = environment.getProperty(GIT_USER, DEFAULT_USER);
    String pwd = environment.getProperty(GIT_PASSWORD, DEFAULT_PWD);
    Git git = Git.cloneRepository().setURI(gitUrl).setDirectory(new File(serviceDir))
        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pwd))
        .setBranch(branch).call();
    git.pull();
  }

  private void createIfNotExist(String serviceDir) {
    File gitDir = new File(serviceDir);
    try {
      if (!gitDir.exists()) {
        FileUtils.createParentDirectories(gitDir);
        return;
      }
      FileUtils.cleanDirectory(gitDir);
    } catch (IOException e) {
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
