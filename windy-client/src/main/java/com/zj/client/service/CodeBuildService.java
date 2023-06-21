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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
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

  @Autowired
  private GitOperator gitOperator;
  @Autowired
  private MavenOperator mavenOperator;
  @Autowired
  @Qualifier("gitOperateExecutor")
  private ExecutorService executorService;
  @Autowired
  private GlobalEnvConfig globalEnvConfig;

  private final Map<String, ResponseModel> statusMap = new ConcurrentHashMap<>();

  public void buildCode(BuildParam buildParam) {
    executorService.execute(() -> {
      try {
        //从git服务端拉取代码
        gitOperator.pullCodeFromGit(buildParam.getGitUrl(), buildParam.getBranch(),
            globalEnvConfig.getGitWorkspace());

        //本地maven构建
        String pomPath = getTargetPomPath(buildParam.getGitUrl(), buildParam.getPomPath());
        String servicePath =
            globalEnvConfig.getGitWorkspace() + File.separator + Utils.getServiceFromUrl(
                buildParam.getGitUrl());
        Integer exitCode = mavenOperator.build(pomPath, servicePath);
        log.info("get maven exit code={}", exitCode);
        ProcessStatus result =
            Objects.equals(0, exitCode) ? ProcessStatus.SUCCESS : ProcessStatus.FAIL;
        saveStatus(buildParam.getRecordId(), result, "构建成功");
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

  private String getTargetPomPath(String gitUrl, String configPath) {
    String serviceName = Utils.getServiceFromUrl(gitUrl);
    return globalEnvConfig.getGitWorkspace() + File.separator + serviceName + File.separator
        + configPath;
  }

  public ResponseModel getRecordStatus(String recordId) {
    ResponseModel responseModel = statusMap.get(recordId);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("status", responseModel.getStatus());
    responseModel.setData(jsonObject);
    return responseModel;
  }
}
