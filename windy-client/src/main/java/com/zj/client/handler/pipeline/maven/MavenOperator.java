package com.zj.client.handler.pipeline.maven;

import com.google.common.base.Preconditions;
import com.zj.client.config.GlobalEnvConfig;
import com.zj.common.exception.ApiException;
import com.zj.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
public class MavenOperator {

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

  public Integer build(String pomPath, String servicePath) throws Exception {
    File pomFile = new File(pomPath);
    InvocationRequest ideaRequest = new DefaultInvocationRequest();
    ideaRequest.setBaseDirectory(new File(servicePath));
    ideaRequest.setAlsoMakeDependents(true);
    ideaRequest.setGoals(Collections.singletonList("package"));

    String mavenDir = globalEnvConfig.getMavenPath();
    Preconditions.checkNotNull(mavenDir, "maven path can not find , consider to fix it");
    Invoker ideaInvoker = new DefaultInvoker();
    ideaInvoker.setMavenHome(new File(mavenDir));
    ideaInvoker.setOutputHandler(System.out::println);
    InvocationResult ideaResult = ideaInvoker.execute(ideaRequest);
    copyJar2DeployDir(pomFile, servicePath);
    return ideaResult.getExitCode();
  }

  /**
   * 将jar文件拷贝到部署目录
   */
  private void copyJar2DeployDir(File pomFile, String servicePath) throws Exception {
    Collection<File> files = FileUtils.listFiles(pomFile.getParentFile(), TrueFileFilter.INSTANCE,
        TrueFileFilter.INSTANCE);
    File jarFile = files.stream().filter(file -> file.isFile() && file.getName().endsWith(".jar"))
        .findAny().orElse(null);
    if (Objects.isNull(jarFile)) {
      throw new ApiException(ErrorCode.NOT_FIND_JAR);
    }

    //ssh镜像部署
    String destDir = servicePath + File.separator + DEPLOY;
    File dir = new File(destDir);
    createSHFileIfNeed(jarFile.getName(), destDir, dir);
    FileUtils.copyToDirectory(jarFile, dir);

    //docker镜像部署
    String dockerDir = servicePath + File.separator + DOCKER;
    File dockerDirFile = new File(dockerDir);
    createSHFileIfNeed(jarFile.getName(), dockerDir, dockerDirFile);
    FileUtils.copyToDirectory(jarFile, dockerDirFile);
  }

  private void createSHFileIfNeed(String jarName, String destDir, File dir) {
    if (!dir.exists()) {
      if(!dir.mkdirs()){
        dir.mkdirs();
      }
    }

    Collection<File> deployFiles = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE,
        TrueFileFilter.INSTANCE);
    File shFile = deployFiles.stream()
        .filter(file -> file.isFile() && file.getName().endsWith(".sh")).findAny().orElse(null);
    if (Objects.isNull(shFile) || !shFile.exists()) {
      createDefaultSHFile(destDir, jarName);
    }
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
