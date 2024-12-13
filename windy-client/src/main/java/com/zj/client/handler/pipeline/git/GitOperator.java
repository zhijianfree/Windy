package com.zj.client.handler.pipeline.git;

import com.zj.client.handler.pipeline.executer.vo.GitMeta;
import com.zj.common.exception.ErrorCode;
import com.zj.common.exception.ExecuteException;
import java.io.PrintWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Slf4j
@Component
public class GitOperator implements IGitProcessor {
  public static final String MASTER = "master";
  public static final String ORIGIN = "origin";

  public Git pullCodeFromGit(GitMeta gitMeta, String branch, String workspace) throws Exception {
    // 判断本地目录是否存在
    createIfNotExist(workspace);

    disableSSLValidation();
    // clone 仓库到指定目录
    return Git.cloneRepository().setURI(gitMeta.getGitUrl()).setDirectory(new File(workspace))
        .setCredentialsProvider(
            new UsernamePasswordCredentialsProvider(gitMeta.getTokenName(), gitMeta.getToken()))
        .setRemote(ORIGIN).setBranch(branch).setProgressMonitor(new TextProgressMonitor(new PrintWriter(System.out))).call();
  }

  private void disableSSLValidation() throws Exception {
    TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
              public X509Certificate[] getAcceptedIssuers() {
                return null;
              }

              public void checkClientTrusted(X509Certificate[] certs, String authType) {
              }

              public void checkServerTrusted(X509Certificate[] certs, String authType) {
              }
            }
    };

    SSLContext sc = SSLContext.getInstance("TLS");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
  }

  private void createIfNotExist(String serviceDir) {
    File gitDir = new File(serviceDir);
    try {
      if (gitDir.exists()) {
        FileUtils.cleanDirectory(gitDir);
        return;
      }
      FileUtils.createParentDirectories(gitDir);
    } catch (IOException e) {
      log.error("create file error", e);
    }
  }

  public MergeResult createTempBranch(GitMeta gitMeta, List<String> branches, String workspace)
      throws Exception {
    Git git = pullCodeFromGit(gitMeta, MASTER, workspace);
    hasDifferencesWithMaster(branches, git);

    String tempBranch = getTempBranchName();
    git.checkout().setCreateBranch(true).setName(tempBranch).call();

    MergeCommand mergeCommand = git.merge().setCommit(true).setFastForward(FastForwardMode.NO_FF)
        .setMessage("Merge temp Branches.");
    getBranchesRef(git, branches).forEach(mergeCommand::include);
    MergeResult mergeResult = mergeCommand.call();
    if (mergeResult.getMergeStatus().isSuccessful()) {
      Ref repositoryRef = git.getRepository().findRef(tempBranch);
      git.push().add(repositoryRef)
          .setCredentialsProvider(
              new UsernamePasswordCredentialsProvider(gitMeta.getTokenName(), gitMeta.getToken()))
          .call();
    }
    return mergeResult;
  }

  /**
   * 如果合并的分支与master无差异不支持发布
   */
  private void hasDifferencesWithMaster(List<String> branches, Git git) throws Exception {
    for (String branch : branches) {
      ObjectId branchHead = git.getRepository().resolve(ORIGIN + "/" + branch);
      ObjectId masterHead = git.getRepository().resolve(ORIGIN + "/" + MASTER);

      // 比较两个分支的差异
      boolean hasDifferences = git.diff()
          .setOldTree(prepareTreeParser(git.getRepository(), masterHead))
          .setNewTree(prepareTreeParser(git.getRepository(), branchHead)).call().iterator()
          .hasNext();

      if (!hasDifferences) {
        String msg = String.format(ErrorCode.BRANCH_NOT_DIFF.getMessage(), branch);
        throw new ExecuteException(msg);
      }
    }
  }

  @Override
  public List<Ref> getBranchesRef(Git git, List<String> branches) {
    Map<String, Ref> allRefs = git.getRepository().getAllRefs();
    return allRefs.keySet().stream().filter(refName -> {
      String branch = parseBranchFromRef(refName);
      return branches.contains(branch);
    }).map(allRefs::get).collect(Collectors.toList());
  }

  private String getTempBranchName() {
    String timeNow = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    return "temp_" + timeNow;
  }

  private String parseBranchFromRef(String ref) {
    int index = ref.lastIndexOf("/");
    return ref.substring(index + 1);
  }

  private static AbstractTreeIterator prepareTreeParser(Repository repository, ObjectId objectId)
      throws IOException {
    try (RevWalk walk = new RevWalk(repository)) {
      RevCommit commit = walk.parseCommit(objectId);
      RevTree tree = walk.parseTree(commit.getTree().getId());

      CanonicalTreeParser treeParser = new CanonicalTreeParser();
      try (ObjectReader objectReader = repository.newObjectReader()) {
        treeParser.reset(objectReader, tree.getId());
      }

      walk.dispose();
      return treeParser;
    }
  }
}
