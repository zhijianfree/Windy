package com.zj.master.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author guyuelan
 * @since 2023/6/15
 */
@Data
public class BuildCodeContext extends RequestContext{

  /**
   * Pom文件的路径
   */
  private String pomPath;

  /**
   * 是否是发布流水线构建
   */
  private Boolean isPublish;

  /**
   * 合并的分支列表
   */
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

  /**
   * 发布的版本
   */
  private String version;

  /**
   * 服务名称
   */
  private String serviceName;
}
