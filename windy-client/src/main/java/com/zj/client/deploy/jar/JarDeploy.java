package com.zj.client.deploy.jar;

import com.zj.client.deploy.IDeployMode;
import com.zj.client.entity.enuns.DeployType;
import org.springframework.stereotype.Component;
import com.jcraft.jsch.*;

/**
 * jar包部署
 *
 * @author guyuelan
 * @since 2023/6/8
 */
@Component("jar")
public class JarDeploy implements IDeployMode<JarDeployContext> {
  private final JSch jsch = new JSch();
  @Override
  public String deployType() {
    return DeployType.JAR.name();
  }

  @Override
  public void deploy(JarDeployContext deployContext) {
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
      channelExec.setCommand("sh " + deployContext.getRemoteSHFile());
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
      System.out.println("Shell脚本执行结果: " + exitStatus);

      channelExec.disconnect();
    } catch (JSchException | SftpException e) {
      e.printStackTrace();
    } finally {
      if (channelSftp != null) {
        channelSftp.disconnect();
      }
      if (session != null) {
        session.disconnect();
      }
    }
  }

}
