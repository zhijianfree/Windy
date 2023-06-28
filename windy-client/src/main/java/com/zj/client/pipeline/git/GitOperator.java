package com.zj.client.pipeline.git;

import com.zj.client.config.GlobalEnvConfig;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
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

  public static final String MASTER = "master";
  @Autowired
  private GlobalEnvConfig globalEnvConfig;

  public Git pullCodeFromGit(String gitUrl, String branch, String workspace)
      throws Exception {
    // 判断本地目录是否存在
    createIfNotExist(workspace);

    // clone 仓库到指定目录
    // 提供用户名和密码的验证
    String user = globalEnvConfig.getGitUser();
    String pwd = globalEnvConfig.getGitPassword();
    Git git = Git.cloneRepository().setURI(gitUrl).setDirectory(new File(workspace))
        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, pwd))
        .setBranch(branch).call();
    git.fetch();
    return git;
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

  public MergeResult createTempBranch(String gitUrl, List<String> branches, String workspace) {
    try {
      Git git = pullCodeFromGit(gitUrl, MASTER, workspace);
      String tempBranch = getTempBranchName();
      git.checkout().setCreateBranch(true).setName(tempBranch).call();

      Map<String, Ref> allRefs = git.getRepository().getAllRefs();
      MergeCommand mergeCommand = git.merge()
          .setCommit(true)
          .setFastForward(FastForwardMode.NO_FF)
          .setMessage("Merge temp Branches.");

      allRefs.keySet().stream().filter(refName -> {
        String branch = parseBranchFromRef(refName);
        return branches.contains(branch);
      }).map(allRefs::get).collect(Collectors.toList()).forEach(mergeCommand::include);
      MergeResult mergeResult = mergeCommand.call();

      if (mergeResult.getMergeStatus().isSuccessful()) {
        Ref repositoryRef = git.getRepository().findRef(tempBranch);
        String user = globalEnvConfig.getGitUser();
        String pwd = globalEnvConfig.getGitPassword();
        git.push().add(repositoryRef).setCredentialsProvider(
            new UsernamePasswordCredentialsProvider(user, pwd)).call();
      }
      return mergeResult;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static String getTempBranchName() {
    String timeNow = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    return "temp_" + timeNow;
  }

  protected String parseBranchFromRef(String ref) {
    int index = ref.lastIndexOf("/");
    return ref.substring(index + 1);
  }
}
