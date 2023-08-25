package com.zj.client.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback.Adapter;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.entity.dto.CodeBuildParamDto;
import com.zj.client.handler.pipeline.executer.notify.PipelineEventFactory;
import com.zj.client.handler.pipeline.executer.vo.PipelineStatusEvent;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel.ResponseStatus;
import com.zj.client.handler.pipeline.executer.vo.TaskNode;
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
  public final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
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

  public void buildCode(CodeBuildParamDto codeBuildParamDto, TaskNode taskNode) {
    saveStatus(codeBuildParamDto.getRecordId(), ProcessStatus.RUNNING, "构建中", null);
    executorService.execute(() -> {
      try {
        //从git服务端拉取代码
        String gitUrl = codeBuildParamDto.getGitUrl();
        String serviceName = Utils.getServiceFromUrl(gitUrl);
        String pipelineWorkspace = globalEnvConfig.getPipelineWorkspace(serviceName,
            codeBuildParamDto.getPipelineId());

        //1 拉取代码到本地
        updateProcessMsg(taskNode, "拉取代码: " + gitUrl);
        updateProcessMsg(taskNode, "拉取分支: " + codeBuildParamDto.getBranches());
        pullCodeFrmGit(codeBuildParamDto, pipelineWorkspace);
        updateProcessMsg(taskNode, "拉取代码完成");

        //2 本地maven构建
        String pomPath = getTargetPomPath(pipelineWorkspace, codeBuildParamDto.getPomPath());
        updateProcessMsg(taskNode, "开始maven打包: " + pomPath);
        Integer exitCode = mavenOperator.build(pomPath, pipelineWorkspace,
            line -> notifyMessage(taskNode, line));
        log.info("get maven exit code={}", exitCode);
        updateProcessMsg(taskNode, "maven构建完成 状态码: " + exitCode);

        //3 构建docker镜像
        updateProcessMsg(taskNode, "开始构建docker镜像");
        String remoteImage = startBuildDocker(serviceName, pipelineWorkspace, codeBuildParamDto);
        updateProcessMsg(taskNode, "构建docker镜像完成 镜像地址: " + remoteImage);

        // 4处理构建结果
        handleBuildResult(codeBuildParamDto, exitCode, remoteImage);
      } catch (Exception e) {
        log.error("buildCode error", e);
        saveStatus(codeBuildParamDto.getRecordId(), ProcessStatus.FAIL, e.getMessage(), null);
      }
    });
  }

  private void handleBuildResult(CodeBuildParamDto codeBuildParamDto, Integer exitCode,
      String remoteImage) {
    ProcessStatus result = Optional.of(exitCode).filter(code -> Objects.equals(BUILD_SUCCESS, code))
        .map(code -> ProcessStatus.SUCCESS).orElse(ProcessStatus.FAIL);
    Map<String, Object> context = new HashMap<>();
    context.put(IMAGE_NAME, remoteImage);
    saveStatus(codeBuildParamDto.getRecordId(), result, BUILD_SUCCESS_TIPS, context);
  }

  /**
   * 只有构建消息才需要运行日志
   */
  private void notifyMessage(TaskNode taskNode, String line) {
    QueryResponseModel model = statusMap.get(taskNode.getRecordId());
    model.addMessage(line);
    PipelineStatusEvent statusEvent = PipelineStatusEvent.builder()
        .taskNode(taskNode)
        .processStatus(ProcessStatus.exchange(model.getStatus()))
        .errorMsg(model.getMessage())
        .context(model.getContext())
        .build();
    PipelineEventFactory.sendNotifyEvent(statusEvent);
  }

  private String startBuildDocker(String serviceName, String pipelineWorkspace,
      CodeBuildParamDto codeBuildParamDto) throws InterruptedException {
    String dateNow = dateFormat.format(new Date());
    String dockerFilePath =
        pipelineWorkspace + File.separator + "docker" + File.separator + "Dockerfile";
    File dockerFile = new File(dockerFilePath);
    return buildDocker(serviceName, dateNow, dockerFile, codeBuildParamDto);
  }

  private void pullCodeFrmGit(CodeBuildParamDto codeBuildParamDto, String pipelineWorkspace)
      throws Exception {
    if (codeBuildParamDto.isPublish()) {
      gitProcessor.createTempBranch(codeBuildParamDto, codeBuildParamDto.getBranches(),
          pipelineWorkspace);
    } else {
      String branch = codeBuildParamDto.getBranches().stream().findFirst().orElse(null);
      gitProcessor.pullCodeFromGit(codeBuildParamDto, branch, pipelineWorkspace);
    }
  }

  private void updateProcessMsg(TaskNode taskNode, String message) {
    notifyMessage(taskNode, "======= " + message);
  }

  private void saveStatus(String recordId, ProcessStatus status, String message,
      Map<String, Object> context) {
    QueryResponseModel model = Optional.ofNullable(statusMap.get(recordId))
        .orElse(new QueryResponseModel());
    model.setStatus(status.getType());
    model.setContext(context);
    model.addMessage(message);

    ResponseStatus responseStatus = new ResponseStatus();
    responseStatus.setStatus(status.getType());
    model.setData(responseStatus);
    statusMap.put(recordId, model);
  }

  private String getTargetPomPath(String pipelineWorkspace, String configPath) {
    return pipelineWorkspace + File.separator + configPath;
  }

  public QueryResponseModel getRecordStatus(String recordId) {
    return statusMap.get(recordId);
  }

  public String buildDocker(String imageName, String version, File dockerfile,
      CodeBuildParamDto param)
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
        .withUsername(param.getUser()).withPassword(param.getPassword());

    // 将镜像推送到远程镜像仓库
    String remoteImage = tagName + SPLIT_STRING + version;
    Adapter<PushResponseItem> responseItemAdapter = dockerClient.pushImageCmd(remoteImage)
        .withAuthConfig(authConfig).start();
    responseItemAdapter.awaitCompletion();
    return remoteImage;
  }
}
