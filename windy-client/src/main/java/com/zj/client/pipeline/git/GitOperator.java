package com.zj.client.pipeline.git;

import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.utils.Utils;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Component
public class GitOperator {

  @Autowired
  private GlobalEnvConfig globalEnvConfig;

  public Git pullCodeFromGit(String gitUrl, String branch, String workspace) throws Exception {
    String serviceName = Utils.getServiceFromUrl(gitUrl);
    // 判断本地目录是否存在
    String serviceDir = workspace + File.separator + serviceName;
    createIfNotExist(serviceDir);

    // clone 仓库到指定目录
    // 提供用户名和密码的验证
    String user = globalEnvConfig.getGitUser();
    String pwd = globalEnvConfig.getGitPassword();
    return Git.cloneRepository().setURI(gitUrl).setDirectory(new File(serviceDir))
        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pwd))
        .setBranch(branch).call();
  }

  private void createIfNotExist(String serviceDir) {
    File gitDir = new File(serviceDir);
    try {
      if (gitDir.exists()) {
        FileUtils.cleanDirectory(gitDir);
        return;
      }
      FileUtils.createParentDirectories(gitDir);
    } catch (IOException ignore) {
    }
  }

  public MergeResult createTempBranch(String repositoryPath, String sourceBranch,
      String targetBranch) {
    try (Repository repository = FileRepositoryBuilder.create(new File(repositoryPath + "/.git"))) {
      Git git = new Git(repository);
      String tempBranch = "temp";
      git.checkout().setName(tempBranch).call();
      git.branchCreate().setName(tempBranch).call();
      git.checkout().setName(tempBranch).call();
      git.push().setCredentialsProvider(
          new UsernamePasswordCredentialsProvider("guyuelan", "zhijian137899")).call();
      Ref sourceRef = repository.findRef(sourceBranch);
      Ref targetRef = repository.findRef(targetBranch);

      // 获取分支的ObjectId
      return git.merge().include(sourceRef).include(targetRef) // 合并源头分支到目标分支
          .setCommit(true)           // 设置合并后同时提交
          .setFastForward(FastForwardMode.NO_FF)// 分支合并策略，--ff代表快速合并， --no-ff代表普通合并
          .setMessage("Merge sourceBranchName into targetBranchName.")     //设置提交信息
          .call();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
