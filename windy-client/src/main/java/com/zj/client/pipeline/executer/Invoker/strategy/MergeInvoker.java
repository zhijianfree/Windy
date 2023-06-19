package com.zj.client.pipeline.executer.Invoker.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.entity.dto.ResponseModel;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
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
  public static final String ORIGIN = "origin";
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
    TempStatus tempStatus = new TempStatus();
    tempStatus.setStatus(ProcessStatus.RUNNING.getType());
    statusMap.put(recordId, tempStatus);
    MergeRequest mergeRequest = JSON
        .parseObject(JSON.toJSONString(requestContext.getData()), MergeRequest.class);
    //拉取远端代码到本地
    Git git = gitOperator.pullCodeFromGit(mergeRequest.getGitUrl(), mergeRequest.getSourceBranch(),
        globalEnvConfig.getGitWorkspace());

    //合并代码
    Repository repository = git.getRepository();
    git.checkout().setName(MASTER).call();
    List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
    Ref sourceRef = findBranchRef(mergeRequest.getSourceBranch(), branches);
    MergeResult mergeResult = git.merge().include(sourceRef)
        // 设置合并后同时提交
        .setCommit(true)
        // 分支合并策略，--ff代表快速合并， --no-ff代表普通合并
        .setFastForward(FastForwardMode.NO_FF)
        //设置提交信息
        .setMessage(String.format(MERGE_COMMIT_TIPS, mergeRequest.getSourceBranch(), MASTER))
        .call();

    //合并成功推送至远端master分支
    if (mergeResult.getMergeStatus().isSuccessful()) {
      log.info("merge success branch ={}", mergeRequest.getSourceBranch());
      boolean result = push2Repository(repository, git);
      tempStatus = new TempStatus();
      tempStatus.setStatus(ProcessStatus.SUCCESS.getType());
      statusMap.put(recordId, tempStatus);
      return result;
    }

    //存在冲突则回退本地的merge状态
    ObjectId objectId = repository.findRef(MASTER).getObjectId();
    git.reset().setMode(ResetCommand.ResetType.HARD).setRef(objectId.getName()).call();

    List<String> fileNames = mergeResult.getConflicts().keySet().stream()
        .map(fileName -> "conflict file: " + fileName)
        .collect(Collectors.toList());
    tempStatus = new TempStatus();
    tempStatus.setStatus(ProcessStatus.SUCCESS.getType());
    tempStatus.setMessage(fileNames);
    statusMap.put(recordId, tempStatus);
    return false;
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
    pushCommand.setRemote(ORIGIN);
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
    TempStatus tempStatus = statusMap.get(recordId);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("status", tempStatus.getStatus());
    ResponseModel responseModel = new ResponseModel();
    responseModel.setStatus(tempStatus.getStatus());
    responseModel.setData(jsonObject);
    responseModel.setMessage(JSON.toJSONString(tempStatus.getMessage()));
    return JSON.toJSONString(responseModel);
  }

  private Ref findBranchRef(String sourceBranch, List<Ref> branches) {
    return branches.stream()
        .filter(ref -> {
          String[] split = ref.getName().split("/");
          return Objects.equals(split[split.length - 1], sourceBranch);
        }).findFirst()
        .orElse(null);
  }
}
