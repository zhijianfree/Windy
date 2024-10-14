package com.zj.master.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Data
public class BuildCodeContext extends RequestContext{

  private String pomPath;

  private Boolean isPublish;

  private List<String> branches;

  /**
   * 镜像仓库地址
   * */
  private String repository;

  /**
   * 推送仓库用户
   * */
  private String user;

  /**
   * 推送仓库密码
   * */
  private String password;

  private String serviceName;
}
