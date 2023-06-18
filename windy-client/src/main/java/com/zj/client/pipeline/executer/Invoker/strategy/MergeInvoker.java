package com.zj.client.pipeline.executer.Invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.pipeline.executer.Invoker.IRemoteInvoker;
import com.zj.client.pipeline.executer.vo.MergeRequest;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.RequestContext;
import com.zj.client.pipeline.executer.vo.TempStatus;
import com.zj.client.pipeline.git.GitOperator;
import com.zj.client.utils.Utils;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.ProcessStatus;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MergeInvoker implements IRemoteInvoker {

  public static final String MASTER = "master";
  public static final String MERGE_COMMIT_TIPS = "Merge %s into %s.";
  private final GitOperator gitOperator;
  private final GlobalEnvConfig globalEnvConfig;
  private final Map<String, TempStatus> statusMap = new ConcurrentHashMap<>();

  public MergeInvoker(GitOperator gitOperator, GlobalEnvConfig globalEnvConfig) {
    this.gitOperator = gitOperator;
    this.globalEnvConfig = globalEnvConfig;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.MERGE;
  }

  @Override
  public boolean triggerRun(RequestContext requestContext, String recordId) throws Exception {
    bindStatus(recordId, ProcessStatus.RUNNING);
    MergeRequest mergeRequest = JSON
        .parseObject(JSON.toJSONString(requestContext.getData()), MergeRequest.class);
    gitOperator.pullCodeFromGit(mergeRequest.getGitUrl(), mergeRequest.getSourceBranch(),
        globalEnvConfig.getGitWorkspace());

    String serviceName = Utils.getServiceFromUrl(mergeRequest.getGitUrl());
    String path = globalEnvConfig.getGitWorkspace() + File.separator + serviceName;
    Repository repository = FileRepositoryBuilder.create(new File(path + "/.git"));
    Git git = new Git(repository);
    git.checkout().setName(MASTER).call();   //切换回被合并分支

    Ref sourceRef = repository.findRef(mergeRequest.getSourceBranch());
    MergeResult mergeResult = git.merge().include(sourceRef)
        // 设置合并后同时提交
        .setCommit(true)
        // 分支合并策略，--ff代表快速合并， --no-ff代表普通合并
        .setFastForward(FastForwardMode.NO_FF)
        //设置提交信息
        .setMessage(String.format(MERGE_COMMIT_TIPS, mergeRequest.getSourceBranch(), MASTER))
        .call();

    if (mergeResult.getMergeStatus().isSuccessful()) {
      bindStatus(recordId, ProcessStatus.SUCCESS);
      log.info("merge success branch ={}", mergeRequest.getSourceBranch());
      return push2Repository(repository, git);
    }

    // 处理合并冲突
    // 获取冲突文件列表
    for (String conflictFile : mergeResult.getConflicts().keySet()) {
      log.info("merge error conflict file={}", conflictFile);
    }
    bindStatus(recordId, ProcessStatus.FAIL);
    return false;
  }

  private void bindStatus(String recordId, ProcessStatus status) {
    TempStatus tempStatus = new TempStatus();
    tempStatus.setStatus(status.getType());
    statusMap.put(recordId, tempStatus);
  }

  private boolean push2Repository(Repository repository, Git git)
      throws IOException, GitAPIException {
    // 更新目标分支的引用
    Ref updatedRef = repository.findRef(MASTER);
    repository.updateRef(updatedRef.getName());

    // 推送合并后的代码到远程仓库的目标分支
    PushCommand pushCommand = git.push();
    pushCommand.setCredentialsProvider(
        new UsernamePasswordCredentialsProvider(globalEnvConfig.getGitUser(),
            globalEnvConfig.getGitPassword()));
    pushCommand.setRemote("origin");
    pushCommand.setRefSpecs(new RefSpec(MASTER));
    Iterable<PushResult> results = pushCommand.call();

    for (PushResult pushResult : results) {
      for (RemoteRefUpdate remoteRefUpdate : pushResult.getRemoteUpdates()) {
        if (remoteRefUpdate.getStatus() == RemoteRefUpdate.Status.OK) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, String recordId) {
    return null;
  }
}
