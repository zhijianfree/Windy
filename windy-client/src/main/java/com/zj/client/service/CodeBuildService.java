package com.zj.client.service;

import com.alibaba.fastjson.JSONObject;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.entity.dto.BuildParam;
import com.zj.client.entity.dto.ResponseModel;
import com.zj.client.pipeline.git.GitOperator;
import com.zj.client.pipeline.maven.MavenOperator;
import com.zj.client.utils.Utils;
import com.zj.common.enums.ProcessStatus;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
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
  @Autowired
  private GitOperator gitOperator;
  @Autowired
  private MavenOperator mavenOperator;
  @Autowired
  @Qualifier("gitOperateExecutor")
  private Executor executorService;
  @Autowired
  private GlobalEnvConfig globalEnvConfig;

  private final Map<String, ResponseModel> statusMap = new ConcurrentHashMap<>();

  public void buildCode(BuildParam buildParam) {
    log.info("before mdc map={}", MDC.getCopyOfContextMap());
    executorService.execute(() -> {
      try {
        log.info("start build code ........ mdc={}", MDC.getCopyOfContextMap());
        //从git服务端拉取代码
        String gitUrl = buildParam.getGitUrl();
        String serviceName = Utils.getServiceFromUrl(gitUrl);
        String pipelineWorkspace = globalEnvConfig.getPipelineWorkspace(serviceName,
            buildParam.getPipelineId());
        Git git = gitOperator.pullCodeFromGit(gitUrl, buildParam.getBranch(), pipelineWorkspace);
        git.fetch();

        //本地maven构建
        String pomPath = getTargetPomPath(pipelineWorkspace, buildParam.getPomPath());
        Integer exitCode = mavenOperator.build(pomPath, pipelineWorkspace);
        log.info("get maven exit code={}", exitCode);
        ProcessStatus result =
            Objects.equals(BUILD_SUCCESS, exitCode) ? ProcessStatus.SUCCESS : ProcessStatus.FAIL;
        saveStatus(buildParam.getRecordId(), result, BUILD_SUCCESS_TIPS);
      } catch (Exception e) {
        log.error("buildCode error", e);
        saveStatus(buildParam.getRecordId(), ProcessStatus.FAIL, e.toString());
      }
    });
    saveStatus(buildParam.getRecordId(), ProcessStatus.RUNNING, "构建中");
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
