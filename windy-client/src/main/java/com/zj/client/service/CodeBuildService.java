package com.zj.client.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.entity.dto.BuildParam;
import com.zj.client.entity.dto.LoopQueryResponse;
import com.zj.client.entity.dto.LoopQueryResponse.ResponseStatus;
import com.zj.client.handler.pipeline.git.IGitProcessor;
import com.zj.client.handler.pipeline.maven.MavenOperator;
import com.zj.client.utils.Utils;
import com.zj.common.enums.ProcessStatus;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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
  public static final String SUFFIX = "/";
  public static final String SPLIT_STRING = ":";
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

  private IGitProcessor gitProcessor;
  private MavenOperator mavenOperator;
  private Executor executorService;
  private GlobalEnvConfig globalEnvConfig;

  private final Map<String, LoopQueryResponse> statusMap = new ConcurrentHashMap<>();

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

        //3 构建docker镜像
        startBuildDocker(serviceName, pipelineWorkspace, buildParam);
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

  private void startBuildDocker(String serviceName, String pipelineWorkspace,
      BuildParam buildParam) {
    String dateNow = DATE_FORMAT.format(new Date());
    String dockerFile =
        pipelineWorkspace + File.separator + "docker" + File.separator + "Dockerfile";
    buildDocker(serviceName, dateNow, dockerFile, buildParam.getRepository(), buildParam.getUser(),
        buildParam.getPassword());
  }

  private void pullCodeFrmGit(BuildParam buildParam, String gitUrl, String pipelineWorkspace)
      throws Exception {
    if (buildParam.getIsPublish()) {
      gitProcessor.createTempBranch(buildParam, buildParam.getBranches(), pipelineWorkspace);
    } else {
      String branch = buildParam.getBranches().get(0);
      gitProcessor.pullCodeFromGit(buildParam, branch, pipelineWorkspace);
    }
  }

  private void saveStatus(String recordId, ProcessStatus status, String message) {
    statusMap.put(recordId, new LoopQueryResponse(status.getType(), message));
  }

  private String getTargetPomPath(String pipelineWorkspace, String configPath) {
    return pipelineWorkspace + File.separator + configPath;
  }

  public LoopQueryResponse getRecordStatus(String recordId) {
    LoopQueryResponse loopQueryResponse = statusMap.get(recordId);
    ResponseStatus responseStatus = new ResponseStatus();
    responseStatus.setStatus(loopQueryResponse.getStatus());
    loopQueryResponse.setData(responseStatus);
    return loopQueryResponse;
  }

  public void buildDocker(String imageName, String version, String dockerFilePath,
      String repository, String user, String password) {
    DockerClient dockerClient = DockerClientBuilder.getInstance().build();
    BuildImageResultCallback callback = new BuildImageResultCallback() {
      @Override
      public void onNext(BuildResponseItem item) {

        // 可以根据需要处理构建的输出
        System.out.println("推送镜像: " + item.getImageId() + "  status: " + item.getStatus());
        super.onNext(item);
      }

      @Override
      public void onError(Throwable throwable) {
        super.onError(throwable);
        System.out.println("构建错误");
        throwable.printStackTrace();
      }
    };
    File dockerfile = new File(dockerFilePath);
    String image = imageName + SPLIT_STRING + version;
    String imageId = dockerClient.buildImageCmd().withDockerfile(dockerfile)
        .withTags(Collections.singleton(image)).exec(callback).awaitImageId();

    String imageUrl = repository.endsWith(SUFFIX) ? repository : SUFFIX + repository;
    String tagName = imageUrl + imageName;
    dockerClient.tagImageCmd(imageId, tagName, version).exec();

    // 设置登陆远程仓库的用户信息
    AuthConfig authConfig = new AuthConfig().withRegistryAddress(repository).withUsername(user)
        .withPassword(password);

    // 将镜像推送到远程镜像仓库
    dockerClient.pushImageCmd(tagName + SPLIT_STRING + version).withAuthConfig(authConfig)
        .exec(new PushImageResultCallback()).awaitSuccess();
  }

}
