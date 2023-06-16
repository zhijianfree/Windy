package com.zj.master.entity.vo;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/6/15
 */
@Data
public class DeployContext extends RequestContext{

  private String executeIp;

  private String remotePath;

  private String sshIp;

  private Integer sshPort;

  private String deployType;
}
