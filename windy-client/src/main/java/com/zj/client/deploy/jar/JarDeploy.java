package com.zj.client.deploy.jar;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.zj.client.deploy.IDeployMode;
import com.zj.client.entity.enuns.DeployType;
import com.zj.common.enums.ProcessStatus;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
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
      channelSftp.put(deployContext.getLocalPath(), deployContext.getRemotePath());

      // 执行远程shell脚本
      ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
      channelExec.setCommand("sh start.sh");
      channelExec.connect();

      // 等待shell脚本执行完成
      while (!channelExec.isClosed()) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      // 获取shell脚本执行结果
      int exitStatus = channelExec.getExitStatus();
      log.info("Shell脚本执行结果: {}", exitStatus);
      bindStatus(deployContext.getRecordId(), ProcessStatus.SUCCESS);
      channelExec.disconnect();
    } catch (JSchException | SftpException e) {
      e.printStackTrace();
      bindStatus(deployContext.getRecordId(), ProcessStatus.FAIL);
    } finally {
      if (channelSftp != null) {
        channelSftp.disconnect();
      }
      if (session != null) {
        session.disconnect();
      }
    }
  }

  @Override
  public ProcessStatus getDeployStatus(String recordId) {
    return statusMap.get(recordId);
  }

  private void bindStatus(String recordId, ProcessStatus status) {
    statusMap.put(recordId, status);
  }

}
