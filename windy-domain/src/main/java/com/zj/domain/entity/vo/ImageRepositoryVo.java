package com.zj.domain.entity.vo;

import lombok.Data;

/**
 * @author falcon
 * @since 2023/8/1
 */
@Data
public class ImageRepositoryVo {

  /**
   * 推送的镜像仓库地址
   * */
  private String repositoryUrl;

  /**
   * 推送镜像用户
   * */
  private String userName;

  /**
   * 推送用户密码
   * */
  private String password;
}
