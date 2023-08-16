package com.zj.client.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback.Adapter;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.entity.dto.BuildParam;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel.ResponseStatus;
import com.zj.client.handler.pipeline.git.IGitProcessor;
import com.zj.client.handler.pipeline.maven.MavenOperator;
import com.zj.client.utils.Utils;
import com.zj.common.enums.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.Closeable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

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
  public static final String[] JAR_FILTER = {".jar"};
  public static final String IMAGE_NAME = "imageName";

  private final IGitProcessor gitProcessor;
  private final MavenOperator mavenOperator;
  private final Executor executorService;
  private final GlobalEnvConfig globalEnvConfig;

  private final Map<String, QueryResponseModel> statusMap = new ConcurrentHashMap<>();

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
        pullCodeFrmGit(buildParam, pipelineWorkspace);

        //2 本地maven构建
        String pomPath = getTargetPomPath(pipelineWorkspace, buildParam.getPomPath());
        Integer exitCode = mavenOperator.build(pomPath, pipelineWorkspace);
        log.info("get maven exit code={}", exitCode);

        //3 构建docker镜像
        String remoteImage = startBuildDocker(serviceName, pipelineWorkspace, buildParam);
        ProcessStatus result =
            Objects.equals(BUILD_SUCCESS, exitCode) ? ProcessStatus.SUCCESS : ProcessStatus.FAIL;
        Map<String, Object> context = new HashMap<>();
        context.put(IMAGE_NAME, remoteImage);
        saveStatus(buildParam.getRecordId(), result, BUILD_SUCCESS_TIPS, context);
      } catch (Exception e) {
        log.error("buildCode error", e);
        saveStatus(buildParam.getRecordId(), ProcessStatus.FAIL, e.getMessage(), null);
      }
    });
    saveStatus(buildParam.getRecordId(), ProcessStatus.RUNNING, "构建中", null);
  }

  private String startBuildDocker(String serviceName, String pipelineWorkspace,
      BuildParam buildParam) throws InterruptedException {
    String dateNow = DATE_FORMAT.format(new Date());
    String dockerFilePath =
        pipelineWorkspace + File.separator + "docker" + File.separator + "Dockerfile";
    File dockerFile = new File(dockerFilePath);
    return buildDocker(serviceName, dateNow, dockerFile, buildParam);
  }

  private void pullCodeFrmGit(BuildParam buildParam, String pipelineWorkspace)
      throws Exception {
    if (buildParam.isPublish()) {
      gitProcessor.createTempBranch(buildParam, buildParam.getBranches(), pipelineWorkspace);
    } else {
      String branch = buildParam.getBranches().stream().findFirst().orElse(null);
      gitProcessor.pullCodeFromGit(buildParam, branch, pipelineWorkspace);
    }
  }

  private void saveStatus(String recordId, ProcessStatus status, String message, Map<String, Object> context) {
    QueryResponseModel queryResponseModel = new QueryResponseModel();
    queryResponseModel.setStatus(status.getType());
    queryResponseModel.setMessage(Collections.singletonList(message));
    queryResponseModel.setContext(context);

    ResponseStatus responseStatus = new ResponseStatus();
    responseStatus.setStatus(status.getType());
    queryResponseModel.setData(responseStatus);
    statusMap.put(recordId, queryResponseModel);
  }

  private String getTargetPomPath(String pipelineWorkspace, String configPath) {
    return pipelineWorkspace + File.separator + configPath;
  }

  public QueryResponseModel getRecordStatus(String recordId) {
    return statusMap.get(recordId);
  }

  public String buildDocker(String imageName, String version, File dockerfile, BuildParam param)
      throws InterruptedException {
    DockerClient dockerClient = DockerClientBuilder.getInstance().build();
    BuildImageResultCallback callback = new BuildImageResultCallback() {
      @Override
      public void onStart(Closeable stream) {
        super.onStart(stream);
        log.info("start build docker image recordId={}", param.getRecordId());
      }

      @Override
      public void onError(Throwable throwable) {
        super.onError(throwable);
        throwable.printStackTrace();
        saveStatus(param.getRecordId(), ProcessStatus.FAIL, "镜像构建失败", null);
      }
    };

    //构建镜像
    String image = imageName + SPLIT_STRING + version;
    String imageId = dockerClient.buildImageCmd().withDockerfile(dockerfile)
        .withTags(Collections.singleton(image)).exec(callback).awaitImageId();

    String repository = param.getRepository();
    //执行docker命令
    String imageUrl = repository.endsWith(SUFFIX) ? repository : repository + SUFFIX;
    String tagName = imageUrl + imageName;
    dockerClient.tagImageCmd(imageId, tagName, version).exec();

    // 设置登陆远程仓库的用户信息
    AuthConfig authConfig = new AuthConfig().withRegistryAddress(repository)
        .withUsername(param.getUser())
        .withPassword(param.getPassword());

    // 将镜像推送到远程镜像仓库
    String remoteImage = tagName + SPLIT_STRING + version;
    Adapter<PushResponseItem> responseItemAdapter = dockerClient.pushImageCmd(remoteImage)
        .withAuthConfig(authConfig)
        .start();
    responseItemAdapter.awaitCompletion();
    return remoteImage;
  }
}
