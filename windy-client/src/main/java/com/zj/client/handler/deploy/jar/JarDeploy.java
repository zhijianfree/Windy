package com.zj.client.handler.deploy.jar;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.zj.client.handler.deploy.IDeployMode;
import com.zj.client.entity.enuns.DeployType;
import com.zj.common.enums.ProcessStatus;
import com.zj.common.exception.ExecuteException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.springframework.stereotype.Component;

/**
 * jar包部署
 *
 * @author guyuelan
 * @since 2023/6/8
 */
@Slf4j
@Component
public class JarDeploy implements IDeployMode<JarDeployContext> {

  private final JSch jsch = new JSch();

  private final Map<String, ProcessStatus> statusMap = new HashMap<>();

  @Override
  public String deployType() {
    return DeployType.JAR.name();
  }

  @Override
  public void deploy(JarDeployContext deployContext) {
    bindStatus(deployContext.getRecordId(), ProcessStatus.RUNNING);
    Session session = null;
    ChannelSftp channelSftp = null;
    try {
      session = jsch.getSession(deployContext.getSshUser(), deployContext.getSshIp(),
          deployContext.getSshPort());
      session.setPassword(deployContext.getSshPassword());
      session.setConfig("StrictHostKeyChecking", "no");
      session.connect();

      channelSftp = (ChannelSftp) session.openChannel("sftp");
      channelSftp.connect();

      // 上传本地JAR文件到远程服务器
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
      }

      // 执行远程shell脚本
      ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
      String shellCommand = "cd " + deployContext.getRemotePath() + " && sh " + shFileName + " "
          + deployContext.getServicePort();
      channelExec.setCommand(shellCommand);
      channelExec.connect();
      channelExec.setErrStream(System.err);

      while (!channelExec.isClosed()) {
        Thread.sleep(1000);
      }

      // 获取shell脚本执行结果
      int exitStatus = channelExec.getExitStatus();
      log.info("execute shell result = {}", exitStatus);
      ProcessStatus status =
          Objects.equals(exitStatus, 0) ? ProcessStatus.SUCCESS : ProcessStatus.FAIL;
      bindStatus(deployContext.getRecordId(), status);
      channelExec.disconnect();
    } catch (Exception e) {
      log.error("execute deploy jar error", e);
      statusMap.remove(deployContext.getRecordId());
      throw new ExecuteException(e.toString());
    } finally {
      if (channelSftp != null) {
        channelSftp.disconnect();
      }
      if (session != null) {
        session.disconnect();
      }
    }
  }

  private boolean isShFile(File file) {
    return file.getName().endsWith(".sh");
  }

  @Override
  public ProcessStatus getDeployStatus(String recordId) {
    return statusMap.get(recordId);
  }

  private void bindStatus(String recordId, ProcessStatus status) {
    statusMap.put(recordId, status);
  }

  private void sendShFile(ChannelSftp channelSftp, String remoteFilePath, File localFile)
      throws Exception {
    // 本地文件输入流
    FileInputStream fis = new FileInputStream(localFile);

    // 远程服务器文件输出流
    OutputStream outputStream = channelSftp.put(remoteFilePath);

    // 使用指定的编码格式进行转换和传输
    BufferedReader reader = new BufferedReader(new InputStreamReader(fis, Charsets.UTF_8));
    BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(outputStream, Charsets.UTF_8));

    String line;
    while ((line = reader.readLine()) != null) {
      writer.write(line);
      writer.newLine();
    }

    writer.flush();

    // 关闭流
    writer.close();
    reader.close();
    outputStream.close();
    fis.close();
  }

}
