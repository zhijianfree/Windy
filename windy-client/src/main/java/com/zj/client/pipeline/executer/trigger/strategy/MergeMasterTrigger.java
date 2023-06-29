package com.zj.client.pipeline.executer.trigger.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.entity.dto.ResponseModel;
import com.zj.client.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.pipeline.executer.vo.MergeRequest;
import com.zj.client.pipeline.executer.vo.MergeStatus;
import com.zj.client.pipeline.executer.vo.RefreshContext;
import com.zj.client.pipeline.executer.vo.TaskNode;
import com.zj.client.pipeline.executer.vo.TriggerContext;
import com.zj.client.pipeline.git.IGitProcessor;
import com.zj.client.utils.Utils;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ErrorCode;
import com.zj.common.exception.ExecuteException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MergeMasterTrigger implements INodeTrigger {

  public static final String MASTER = "master";
  public static final String MERGE_COMMIT_TIPS = "Merge master";
  public static final String ORIGIN = "origin";
  public static final String MERGER_ERROR_TIPS = "merger error, find conflict files: ";
  private final IGitProcessor gitProcessor;
  private final GlobalEnvConfig globalEnvConfig;
  private final Map<String, MergeStatus> statusMap = new ConcurrentHashMap<>();

  public MergeMasterTrigger(IGitProcessor gitProcessor, GlobalEnvConfig globalEnvConfig) {
    this.gitProcessor = gitProcessor;
    this.globalEnvConfig = globalEnvConfig;
  }

  @Override
  public ExecuteType type() {
    return ExecuteType.MERGE;
  }

  @Override
  public void triggerRun(TriggerContext triggerContext, TaskNode taskNode) throws Exception {
    statusMap.put(taskNode.getRecordId(), new MergeStatus(ProcessStatus.RUNNING.getType()));
    try {
      //1 拉取代码到本地
      MergeRequest mergeRequest = JSON.parseObject(JSON.toJSONString(triggerContext.getData()),
          MergeRequest.class);
      String serviceName = Utils.getServiceFromUrl(mergeRequest.getGitUrl());
      Git git = gitProcessor.pullCodeFromGit(mergeRequest.getGitUrl(), MASTER,
          globalEnvConfig.getPipelineWorkspace(serviceName, mergeRequest.getPipelineId()));

      //2 合并代码
      MergeCommand mergeCommand = git.merge().setCommit(true).setFastForward(FastForwardMode.NO_FF)
          .setMessage(MERGE_COMMIT_TIPS);
      Repository repository = git.getRepository();
      List<Ref> branchesRef = gitProcessor.getBranchesRef(git, mergeRequest.getBranches());
      branchesRef.forEach(mergeCommand::include);
      MergeResult mergeResult = mergeCommand.call();

      //合并成功推送至远端master分支
      if (mergeResult.getMergeStatus().isSuccessful()) {
        log.info("merge success branches ={}", mergeRequest.getBranches());
        push2Repository(repository, git);
        deleteBranch(mergeRequest, branchesRef, git);
        statusMap.put(taskNode.getRecordId(), new MergeStatus(ProcessStatus.SUCCESS.getType()));
        return;
      }

      //存在冲突则回退本地的merge状态
      ObjectId objectId = repository.findRef(MASTER).getObjectId();
      git.reset().setMode(ResetCommand.ResetType.HARD).setRef(objectId.getName()).call();

      List<String> fileNames = new ArrayList<>();
      fileNames.add(MERGER_ERROR_TIPS);
      fileNames.addAll(mergeResult.getConflicts().keySet());
      statusMap.put(taskNode.getRecordId(),
          new MergeStatus(ProcessStatus.FAIL.getType(), fileNames));
    } catch (Exception e) {
      log.warn("merger code error", e);
      statusMap.put(taskNode.getRecordId(),
          new MergeStatus(ProcessStatus.FAIL.getType(), Collections.singletonList(e.toString())));
    }
  }

  private void deleteBranch(MergeRequest mergeRequest, List<Ref> branchesRef, Git git)
      throws GitAPIException {
    if (!mergeRequest.isDeleteBranch()) {
      return;
    }

    for (Ref ref : branchesRef) {
      git.branchDelete().setBranchNames(ref.getName()).setForce(true).call();
      git.push().setRefSpecs(new RefSpec().setSource(ref.getName())).setRemote(ORIGIN).call();
    }
  }

  private void push2Repository(Repository repository, Git git) throws IOException, GitAPIException {
    // 推送合并后的代码到远程仓库的目标分支
    Ref updatedRef = repository.findRef(MASTER);
    Iterable<PushResult> results = git.push().add(updatedRef).setCredentialsProvider(
        new UsernamePasswordCredentialsProvider(globalEnvConfig.getGitUser(),
            globalEnvConfig.getGitPassword())).setRemote(ORIGIN).call();
    boolean pushStatus = StreamSupport.stream(results.spliterator(), false).anyMatch(
        pushResult -> pushResult.getRemoteUpdates().stream()
            .anyMatch(remoteRefUpdate -> Objects.equals(remoteRefUpdate.getStatus(), Status.OK)));
    if (!pushStatus) {
      throw new ExecuteException(ErrorCode.MERGE_CODE_ERROR);
    }
  }

  @Override
  public String queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
    MergeStatus mergeStatus = statusMap.get(taskNode.getRecordId());
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("status", mergeStatus.getStatus());
    ResponseModel responseModel = new ResponseModel();
    responseModel.setStatus(mergeStatus.getStatus());
    responseModel.setData(jsonObject);
    responseModel.setMessage(JSON.toJSONString(mergeStatus.getMessage()));
    return JSON.toJSONString(responseModel);
  }
}
