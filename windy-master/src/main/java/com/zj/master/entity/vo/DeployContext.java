package com.zj.master.entity.vo;

import lombok.Data;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Data
public class DeployContext extends RequestContext{

  private String envId;

  private Object params;

  private Integer deployType;

  private Integer serverPort;

  private String imageName;

  private String buildFilePath;

}
