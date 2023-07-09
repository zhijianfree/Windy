package com.zj.service.entity;

import lombok.Data;

@Data
public class SSHParams {

  /**
   * ssh的IP
   * */
  private String host;

  /**
   * ssh 端口
   * */
  private Integer port;

  /**
   * ssh 用户
   * */
  private String user;

  /**
   * ssh 密码
   * */
  private String password;
}
