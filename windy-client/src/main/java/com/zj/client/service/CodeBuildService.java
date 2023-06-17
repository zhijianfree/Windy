package com.zj.client.service;

import com.alibaba.fastjson.JSONObject;
import com.zj.client.entity.dto.BuildParam;
import com.zj.client.entity.dto.ResponseModel;
import com.zj.client.pipeline.git.GitOperator;
import com.zj.client.pipeline.maven.MavenOperator;
import com.zj.common.enums.ProcessStatus;
import java.io.File;
import java.io.IOException;
import java.util.Map;
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

  public static final String GIT_WORKSPACE = "windy.pipeline.git.workspace";
  public static final String DEFAULT_WORKSPACE = "/opt/windy";
  public static final String WINDY = "windy";
  @Autowired
  private GitOperator gitOperator;
  @Autowired
  private MavenOperator mavenOperator;
  @Autowired
  @Qualifier("gitOperateExecutor")
  private ExecutorService executorService;

  private final Map<String, ResponseModel> statusMap = new ConcurrentHashMap<>();
  private final String workspace;

  public CodeBuildService(Environment environment) {
    workspace = getWorkSpace(environment);
  }

  public Boolean buildCode(BuildParam buildParam) {
    executorService.execute(() -> {
      try {
        //从git服务端拉取代码
        gitOperator.pullCodeFromGit(buildParam.getGitUrl(), buildParam.getBranch(), workspace);

        //本地maven构建
        String pomPath = getTargetPomPath(buildParam.getGitUrl(), buildParam.getPomPath());
        Integer exitCode = mavenOperator.build(pomPath);
        log.info("get maven exit code={}", exitCode);
        saveStatus(buildParam.getRecordId(), ProcessStatus.SUCCESS.getType(), "构建成功");
      } catch (Exception e) {
        log.error("buildCode error", e);
        saveStatus(buildParam.getRecordId(), ProcessStatus.FAIL.getType(), e.toString());
      }
    });
    saveStatus(buildParam.getRecordId(), ProcessStatus.RUNNING.getType(), "构建中");
    return true;
  }

  private void saveStatus(String recordId, int status, String message) {
    statusMap.put(recordId, new ResponseModel(status, message));
  }

  private String getTargetPomPath(String gitUrl, String configPath) {
    String serviceName = GitOperator.getServiceFromUrl(gitUrl);
    return workspace + File.separator + serviceName + File.separator + configPath;
  }

  public ResponseModel getRecordStatus(String recordId) {
    ResponseModel responseModel = statusMap.get(recordId);
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("status", responseModel.getStatus());
    responseModel.setData(jsonObject);
    return responseModel;
  }

  private String getWorkSpace(Environment environment) {
    String path = environment.getProperty(GIT_WORKSPACE, DEFAULT_WORKSPACE);
    if (!isWorkspaceExist(path)) {
      try {
        path = new File("").getCanonicalPath() + File.separator + WINDY;
      } catch (IOException ignore) {
      }
    }
    return path;
  }

  private boolean isWorkspaceExist(String workspace) {
    return new File(workspace).exists();
  }
}
