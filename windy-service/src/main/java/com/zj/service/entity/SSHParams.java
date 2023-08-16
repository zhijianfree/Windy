package com.zj.service.entity;

import lombok.Data;

@Data
public class SSHParams {

  /**
   * ssh的IP
   * */
  private String sshIp;

  /**
   * ssh 端口
   * */
  private Integer sshPort;

  /**
   * ssh 用户
   * */
  private String user;

  /**
   * ssh 密码
   * */
  private String password;
}
