package com.zj.client.handler.pipeline.executer.trigger.strategy;

import com.alibaba.fastjson.JSON;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.handler.pipeline.executer.trigger.INodeTrigger;
import com.zj.client.handler.pipeline.executer.vo.MergeRequest;
import com.zj.client.handler.pipeline.executer.vo.MergeStatus;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel.ResponseStatus;
import com.zj.client.handler.pipeline.executer.vo.RefreshContext;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
import com.zj.client.handler.pipeline.executer.vo.TriggerContext;
import com.zj.client.handler.pipeline.git.IGitProcessor;
import com.zj.client.utils.Utils;
import com.zj.common.enums.ExecuteType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ErrorCode;
import com.zj.common.exception.ExecuteException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * 合并master
 */
@Slf4j
@Component
public class MergeMasterTrigger implements INodeTrigger {

    public static final String MASTER = "master";
    public static final String ORIGIN = "origin";
    public static final String MERGER_ERROR_TIPS = "merger error, find conflict files: ";
    public static final String REFS_HEADS = "refs/heads/";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-");
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
            Git git = gitProcessor.pullCodeFromGit(mergeRequest, MASTER,
                    globalEnvConfig.getPipelineWorkspace(serviceName, mergeRequest.getPipelineId()));

            //2 合并代码
            MergeCommand mergeCommand = git.merge();
            List<Ref> branchesRef = gitProcessor.getBranchesRef(git, mergeRequest.getBranches());
            branchesRef.forEach(mergeCommand::include);
            MergeResult mergeResult = mergeCommand.call();

            //合并成功推送至远端master分支
            if (mergeResult.getMergeStatus().isSuccessful()) {
                log.info("merge success branches ={}", mergeRequest.getBranches());
                push2Repository(mergeRequest, git);
                createCodeTag(mergeRequest, git);
                deleteBranch(mergeRequest, git);
                statusMap.put(taskNode.getRecordId(), new MergeStatus(ProcessStatus.SUCCESS.getType()));
                return;
            }

            //存在冲突则回退本地的merge状态
            ObjectId objectId = git.getRepository().findRef(MASTER).getObjectId();
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(objectId.getName()).call();

            List<String> fileNames = new ArrayList<>();
            fileNames.add(MERGER_ERROR_TIPS);
            fileNames.addAll(mergeResult.getConflicts().keySet());
            statusMap.put(taskNode.getRecordId(),
                    new MergeStatus(ProcessStatus.FAIL.getType(), fileNames));
        } catch (Exception e) {
            log.warn("merger code error", e);
            statusMap.put(taskNode.getRecordId(),
                    new MergeStatus(ProcessStatus.FAIL.getType(), Collections.singletonList(e.getMessage())));
        }
    }

    private void createCodeTag(MergeRequest mergeRequest, Git git) {
        log.info("start create tag ={}", mergeRequest.getTagName());
        try {
            String date = dateFormat.format(new Date());
            git.tag().setName(date + mergeRequest.getTagName()).setMessage(mergeRequest.getMessage()).call();
            git.push().setCredentialsProvider(getCredentialsProvider(mergeRequest.getTokenName(),
                    mergeRequest.getToken())).setPushTags().call();
        } catch (Exception e) {
            log.info("create code tag error", e);
        }
    }

    private void deleteBranch(MergeRequest mergeRequest, Git git) throws GitAPIException {
        if (!mergeRequest.isDeleteBranch()) {
            return;
        }
        List<String> remoteRefNames = mergeRequest.getBranches().stream()
                .map(branch -> REFS_HEADS + branch).collect(Collectors.toList());
        List<String> strings = git.branchDelete().setBranchNames(remoteRefNames.toArray(new String[]{}))
                .setForce(true).call();

        List<RefSpec> refSpecs = mergeRequest.getBranches().stream().map(
                branch -> new RefSpec().setSource(null).setForceUpdate(true)
                        .setDestination(REFS_HEADS + branch)).collect(Collectors.toList());
        git.push().setRefSpecs(refSpecs).setRemote(ORIGIN)
                .setCredentialsProvider(getCredentialsProvider(mergeRequest.getTokenName(),
                        mergeRequest.getToken())).call();
        log.info("delete branches remoteRefNames={} result={}", remoteRefNames, strings);
    }

    private void push2Repository(MergeRequest mergeRequest, Git git)
            throws GitAPIException {
        // 推送合并后的代码到远程仓库的目标分支
        Iterable<PushResult> results = git.push().setRemote(ORIGIN).setRefSpecs(new RefSpec(MASTER))
                .setCredentialsProvider(
                        getCredentialsProvider(mergeRequest.getTokenName(), mergeRequest.getToken())).call();
        boolean pushStatus = StreamSupport.stream(results.spliterator(), false).anyMatch(
                pushResult -> pushResult.getRemoteUpdates().stream()
                        .anyMatch(remoteRefUpdate -> Objects.equals(remoteRefUpdate.getStatus(), Status.OK)));
        if (!pushStatus) {
            throw new ExecuteException(ErrorCode.MERGE_CODE_ERROR);
        }
    }

    private UsernamePasswordCredentialsProvider getCredentialsProvider(String tokenName,
                                                                       String token) {
        return new UsernamePasswordCredentialsProvider(tokenName, token);
    }

    @Override
    public QueryResponseModel queryStatus(RefreshContext refreshContext, TaskNode taskNode) {
        MergeStatus mergeStatus = statusMap.get(taskNode.getRecordId());
        QueryResponseModel loopQueryResponse = new QueryResponseModel();
        loopQueryResponse.setStatus(mergeStatus.getStatus());
        loopQueryResponse.setData(new ResponseStatus(loopQueryResponse.getStatus()));
        loopQueryResponse.setMessage(mergeStatus.getMessage());
        return loopQueryResponse;
    }
}
