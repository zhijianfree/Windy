package com.zj.client.service;

import com.alibaba.fastjson.JSONObject;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.entity.dto.BuildParam;
import com.zj.client.entity.dto.ResponseModel;
import com.zj.client.handler.pipeline.git.IGitProcessor;
import com.zj.client.handler.pipeline.maven.MavenOperator;
import com.zj.client.utils.Utils;
import com.zj.common.enums.ProcessStatus;
import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Slf4j
@Service
public class CodeBuildService {

  public static final int BUILD_SUCCESS = 0;
  public static final String BUILD_SUCCESS_TIPS = "构建成功";

  private IGitProcessor gitProcessor;
  private MavenOperator mavenOperator;
  private Executor executorService;
  private GlobalEnvConfig globalEnvConfig;

  private final Map<String, ResponseModel> statusMap = new ConcurrentHashMap<>();

  public CodeBuildService(IGitProcessor gitProcessor, MavenOperator mavenOperator,
      @Qualifier("gitOperatePool") Executor executorService, GlobalEnvConfig globalEnvConfig) {
    this.gitProcessor = gitProcessor;
    this.mavenOperator = mavenOperator;
    this.executorService = executorService;
    this.globalEnvConfig = globalEnvConfig;
  }

  public void buildCode(BuildParam buildParam) {
    executorService.execute(() -> {
      try {
        //从git服务端拉取代码
        String gitUrl = buildParam.getGitUrl();
        String serviceName = Utils.getServiceFromUrl(gitUrl);
        String pipelineWorkspace = globalEnvConfig.getPipelineWorkspace(serviceName,
            buildParam.getPipelineId());

        //1 拉取代码到本地
        pullCodeFrmGit(buildParam, gitUrl, pipelineWorkspace);

        //2 本地maven构建
        String pomPath = getTargetPomPath(pipelineWorkspace, buildParam.getPomPath());
        Integer exitCode = mavenOperator.build(pomPath, pipelineWorkspace);
        log.info("get maven exit code={}", exitCode);
        ProcessStatus result =
            Objects.equals(BUILD_SUCCESS, exitCode) ? ProcessStatus.SUCCESS : ProcessStatus.FAIL;
        saveStatus(buildParam.getRecordId(), result, BUILD_SUCCESS_TIPS);
      } catch (Exception e) {
        log.error("buildCode error", e);
        saveStatus(buildParam.getRecordId(), ProcessStatus.FAIL, e.getMessage());
      }
    });
    saveStatus(buildParam.getRecordId(), ProcessStatus.RUNNING, "构建中");
  }

  private void pullCodeFrmGit(BuildParam buildParam, String gitUrl, String pipelineWorkspace)
      throws Exception {
    if (buildParam.getIsPublish()) {
      gitProcessor.createTempBranch(gitUrl, buildParam.getBranches(), pipelineWorkspace);
    } else {
      String branch = buildParam.getBranches().get(0);
      gitProcessor.pullCodeFromGit(gitUrl, branch, pipelineWorkspace);
    }
  }

  private void saveStatus(String recordId, ProcessStatus status, String message) {
    statusMap.put(recordId, new ResponseModel(status.getType(), message));
  }

  private String getTargetPomPath(String pipelineWorkspace, String configPath) {
    return pipelineWorkspace + File.separator + configPath;
  }

  public ResponseModel getRecordStatus(String recordId) {
    ResponseModel responseModel = statusMap.get(recordId);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("status", responseModel.getStatus());
    responseModel.setData(jsonObject);
    return responseModel;
  }
}
