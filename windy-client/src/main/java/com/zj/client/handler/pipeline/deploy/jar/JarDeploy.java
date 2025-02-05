package com.zj.client.handler.pipeline.deploy.jar;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.zj.client.handler.pipeline.deploy.AbstractDeployMode;
import com.zj.client.handler.pipeline.executer.vo.QueryResponseModel;
import com.zj.client.utils.ExceptionUtils;
import com.zj.common.enums.DeployType;
import com.zj.common.enums.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * jar包部署
 *
 * @author guyuelan
 * @since 2023/6/8
 */
@Slf4j
@Component
public class JarDeploy extends AbstractDeployMode<JarDeployContext> {

  private final JSch jsch = new JSch();

  @Override
  public Integer deployType() {
    return DeployType.SSH.getType();
  }

  @Override
  public void deploy(JarDeployContext deployContext) {
    updateDeployStatus(deployContext.getRecordId(), ProcessStatus.RUNNING);
    Session session = null;
    ChannelSftp channelSftp = null;
    try {
      session = connectServer(deployContext);
      //创建目录
      ChannelExec channel = (ChannelExec) session.openChannel("exec");
      channel.setCommand("mkdir -p " + deployContext.getRemotePath());
      channel.connect();

      // 上传本地JAR文件到远程服务器
      channelSftp = (ChannelSftp) session.openChannel("sftp");
      channelSftp.connect();
      String shFileName = uploadFiles(deployContext, channelSftp);

      // 执行远程shell脚本
      ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
      executeShell(deployContext, shFileName, channelExec);
      while (!channelExec.isClosed()) {
        Thread.sleep(3000);
      }

      // 获取shell脚本执行结果
      int exitStatus = channelExec.getExitStatus();
      channelExec.disconnect();
      log.info("execute shell result = {}", exitStatus);
      updateDeployStatus(deployContext.getRecordId(), ProcessStatus.RUNNING, Collections.singletonList(
              "=====执行脚本结果：" + exitStatus));
      ProcessStatus status = Optional.of(exitStatus).filter(s -> Objects.equals(exitStatus, 0))
          .map(s -> ProcessStatus.SUCCESS).orElse(ProcessStatus.FAIL);
      updateDeployStatus(deployContext.getRecordId(), status);
    } catch (Exception e) {
      log.error("execute deploy jar error", e);
      updateDeployStatus(deployContext.getRecordId(), ProcessStatus.FAIL, ExceptionUtils.getErrorMsg(e));
    } finally {
      if (channelSftp != null) {
        channelSftp.disconnect();
      }
      if (session != null) {
        session.disconnect();
      }
    }
  }

  private Session connectServer(JarDeployContext deployContext) throws JSchException {
    Session session = jsch.getSession(deployContext.getSshUser(), deployContext.getSshIp(),
            deployContext.getSshPort());
    session.setPassword(deployContext.getSshPassword());
    session.setConfig("StrictHostKeyChecking", "no");
    session.connect();
    updateDeployStatus(deployContext.getRecordId(), ProcessStatus.RUNNING, Collections.singletonList(
            "=====已连接到远程SSH服务"));
    return session;
  }

  private void executeShell(JarDeployContext deployContext, String shFileName, ChannelExec channelExec) throws JSchException {
    String port = Optional.ofNullable(deployContext.getServicePort()).map(String::valueOf).orElse("");
    String shellCommand = "cd " + deployContext.getRemotePath() + " && sh " + shFileName + " " + port;
    channelExec.setCommand(shellCommand);
    channelExec.connect();
    updateDeployStatus(deployContext.getRecordId(), ProcessStatus.RUNNING, Collections.singletonList(
            "=====已执行启动脚本"));
  }

  private String uploadFiles(JarDeployContext deployContext, ChannelSftp channelSftp) throws Exception {
    String shFileName = "";
    Collection<File> files = FileUtils.listFiles(new File(deployContext.getLocalPath()),
        TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    for (File file : files) {
      if (file.isDirectory()) {
        continue;
      }
      String remoteFile = deployContext.getRemotePath() + File.separator + file.getName();
      if (isShFile(file)) {
        shFileName = file.getName();
        sendShFile(channelSftp, remoteFile, file);
        continue;
      }

      FileInputStream fis = new FileInputStream(file);
      channelSftp.put(fis, remoteFile, ChannelSftp.OVERWRITE);
      fis.close();
      updateDeployStatus(deployContext.getRecordId(), ProcessStatus.RUNNING, Collections.singletonList(
              "=====推送至远程服务: " + file.getName()));
    }
    return shFileName;
  }

  private boolean isShFile(File file) {
    return file.getName().endsWith(".sh");
  }

  @Override
  public QueryResponseModel getDeployStatus(String recordId) {
    return statusMap.get(recordId);
  }

  private void sendShFile(ChannelSftp channelSftp, String remoteFilePath, File localFile)
      throws Exception {
    try (FileInputStream fis = new FileInputStream(
        localFile); OutputStream outputStream = channelSftp.put(
        remoteFilePath); BufferedReader reader = new BufferedReader(
        new InputStreamReader(fis, StandardCharsets.UTF_8))) {
      BufferedWriter writer = new BufferedWriter(
          new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

      String line;
      while ((line = reader.readLine()) != null) {
        writer.write(line);
        writer.newLine();
      }
      writer.flush();
    }
  }
}
