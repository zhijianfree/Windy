package com.zj.client.handler.pipeline.build.maven;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.client.handler.pipeline.build.CodeBuildContext;
import com.zj.client.handler.pipeline.build.IBuildNotifyListener;
import com.zj.client.handler.pipeline.build.ICodeBuilder;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.common.enums.CodeType;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.shared.invoker.*;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Slf4j
@Component
public class MavenOperator implements ICodeBuilder {

  public static final String DEPLOY = "deploy";
  public static final String SH_COMMAND_FORMAT = "nohup java -jar %s > app.log 2>&1 &";
  public static final String DOCKER = "docker";
  private final GlobalEnvConfig globalEnvConfig;
  private List<String> templateShell;

  public MavenOperator(GlobalEnvConfig globalEnvConfig) {
    this.globalEnvConfig = globalEnvConfig;
    try {
      URL resourceURL = ResourceUtils.getURL("classpath:start.sh");
      InputStream inputStream = resourceURL.openStream();
      templateShell = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
      inputStream.close();
    } catch (Exception e) {
      log.warn("load template sh file error", e);
    }
  }

  @Override
  public String codeType() {
    return CodeType.JAVA.getType();
  }

  @Override
  public Integer build(CodeBuildContext context, IBuildNotifyListener notifyListener) {
    try {
      log.info("start build java code = {}", JSON.toJSONString(context));
      return build(context.getBuildFile(), context.getTargetDir(), notifyListener::notifyMessage);
    } catch (Exception e) {
      log.info("execute maven error", e);
    }
    return -1;
  }

  public Integer build(String pomPath, String servicePath, InvocationOutputHandler outputHandler)
      throws IOException, MavenInvocationException {
    InvocationRequest ideaRequest = new DefaultInvocationRequest();
    ideaRequest.setBaseDirectory(new File(servicePath));
    ideaRequest.setAlsoMakeDependents(true);
    ideaRequest.setGoals(Collections.singletonList("package"));

    String mavenDir = globalEnvConfig.getMavenPath();
    Preconditions.checkNotNull(mavenDir, "maven path can not find , consider to fix it");
    Invoker ideaInvoker = new DefaultInvoker();
    ideaInvoker.setMavenHome(new File(mavenDir));
    ideaInvoker.setOutputHandler(outputHandler);
    InvocationResult ideaResult = ideaInvoker.execute(ideaRequest);
    File pomFile = new File(pomPath);
    copyJar2DeployDir(pomFile);
    return ideaResult.getExitCode();
  }

  /**
   * 将jar文件拷贝到部署目录
   */
  private void copyJar2DeployDir(File pomFile) throws IOException {
    Collection<File> files = FileUtils.listFiles(pomFile.getParentFile(), new String[]{"jar"} ,true);
    File jarFile = files.stream().findFirst().orElse(null);
    if (Objects.isNull(jarFile)) {
      throw new ApiException(ErrorCode.NOT_FIND_JAR);
    }

    //ssh镜像部署
    String destDir = pomFile.getParentFile().getPath() + File.separator + DEPLOY;
    File dir = new File(destDir);
    createSHFileIfNeed(jarFile.getName(), destDir, dir);
    FileUtils.copyToDirectory(jarFile, dir);

    //docker镜像部署
    String dockerDir = pomFile.getParentFile().getPath() + File.separator + DOCKER;
    File dockerDirFile = new File(dockerDir);
    createSHFileIfNeed(jarFile.getName(), dockerDir, dockerDirFile);
    FileUtils.copyToDirectory(jarFile, dockerDirFile);
  }

  private void createSHFileIfNeed(String jarName, String destDir, File dir) throws IOException {
    if (!dir.exists()) {
      boolean result = dir.mkdirs();
      log.debug("creat sh file parent path result={}", result);
    }

    Collection<File> shFiles = FileUtils.listFiles(dir, new String[]{"sh"} ,false);
    if (CollectionUtils.isNotEmpty(shFiles)) {
      log.info("destination dir={} hava sh file, not create default sh file", destDir);
      return;
    }
    createDefaultSHFile(destDir, jarName);
  }

  private void createDefaultSHFile(String destDir, String name) {
    try {
      File destFile = new File(destDir + File.separator + "start.sh");
      List<String> commands = new ArrayList<>(templateShell);
      String command = String.format(SH_COMMAND_FORMAT, name);
      commands.add(command);
      FileUtils.writeLines(destFile, StandardCharsets.UTF_8.name(), commands, "\r\n", true);
    } catch (IOException e) {
      log.error("write event to file error", e);
    }
  }
}
